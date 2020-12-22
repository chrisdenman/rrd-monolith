package uk.co.ceilingcat.rrd.monolith

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APP_TEST_PROPERTIES_PATH
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.PROPERTIES_PATH_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.assertEquals
import uk.co.ceilingcat.rrd.monolith.test.nonExistentDirectory
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MainMainSpec {

    @Test
    fun `That we can run the app by calling it's static main() method as in production`() {
        System.setProperty(
            PROPERTIES_PATH_PROPERTY_NAME,
            File(File(APP_TEST_PROPERTIES_PATH), "test").canonicalPath
        )
        Unit assertEquals Main.main(emptyList<String>().toTypedArray())
    }

    @Test
    fun `That we can run the app by calling it's static main() method as in production and it returns Unit even if there was an error`() {
        System.setProperty(PROPERTIES_PATH_PROPERTY_NAME, nonExistentDirectory.canonicalPath)
        Unit assertEquals Main.main(emptyList<String>().toTypedArray())
    }
}
