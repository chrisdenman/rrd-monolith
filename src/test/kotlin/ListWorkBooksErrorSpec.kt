package uk.co.ceilingcat.rrd.monolith

import arrow.core.flatMap
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS.EMPTY
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.XlsxHelper.Companion.withSheet
import uk.co.ceilingcat.rrd.monolith.test.assertLeft
import uk.co.ceilingcat.rrd.monolith.test.set
import java.io.FilePermission
import java.lang.System.setSecurityManager
import java.security.Permission

@TestInstance(PER_CLASS)
class ListWorkBooksErrorSpec {

    @Test
    fun `That is we can't list the files in the spreadsheet search directory due to a security manager exception, we get a MainExecutionError as it's our sole input gateway`() {
        val systemSecurityManager = System.getSecurityManager()

        MainExecutionError assertLeft withSheet(EMPTY).parentFile.let { spreadsheetDirectory ->
            PropertiesHelper.mutateProperties({ properties ->
                properties.set(
                    mapOf(WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath)
                )
            }).flatMap { (bootstrapProperties, _) ->
                setSecurityManager(
                    object : SecurityManager() {
                        var count = 0
                        override fun checkPermission(perm: Permission?) {
                            if (perm == FilePermission(spreadsheetDirectory.canonicalPath, "read") &&
                                (count++ == 2) // We have to account for property validation (exists && canRead)
                            ) {
                                throw SecurityException()
                            }
                        }
                    }
                )

                Main((bootstrapProperties)).execute
            }
        }

        setSecurityManager(systemSecurityManager)
    }
}
