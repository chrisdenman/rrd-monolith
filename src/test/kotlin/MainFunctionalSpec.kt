package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either.Companion.right
import arrow.core.flatMap
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.CalendarName
import uk.co.ceilingcat.rrd.monolith.test.AllInputGatewaysFail
import uk.co.ceilingcat.rrd.monolith.test.AllOutputGatewaysFail
import uk.co.ceilingcat.rrd.monolith.test.CalendarHelper.Companion.eventFoundWithSummary
import uk.co.ceilingcat.rrd.monolith.test.CalendarHelper.Companion.eventFoundWithSummaryAndRemoved
import uk.co.ceilingcat.rrd.monolith.test.CellAddress
import uk.co.ceilingcat.rrd.monolith.test.CellData
import uk.co.ceilingcat.rrd.monolith.test.MailHelper.Companion.mailFoundAndRemoved
import uk.co.ceilingcat.rrd.monolith.test.NoInputGateways
import uk.co.ceilingcat.rrd.monolith.test.NoOutputGateways
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.mutateProperties
import uk.co.ceilingcat.rrd.monolith.test.SheetAddress
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DAY_HENCE_NOTIFIED_OF
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_PASSWORD_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_USER_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.INPUT_GATEWAY_PATTERN_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.MAIL_CHECK_TIMEOUT_MS
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.MAIL_CHECK_WAIT_MS
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.STREET_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.SUBJECT_TEMPLATE_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS.EMPTY
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS.HISTORIC
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS.MALFORMED
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.XLSX_INPUT_GATEWAY_PROPERTY_VALUE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.worksheetFile
import uk.co.ceilingcat.rrd.monolith.test.XlsxHelper.Companion.withEmpty
import uk.co.ceilingcat.rrd.monolith.test.XlsxHelper.Companion.withRefuseDaysHence
import uk.co.ceilingcat.rrd.monolith.test.XlsxHelper.Companion.withSheet
import uk.co.ceilingcat.rrd.monolith.test.assertLeft
import uk.co.ceilingcat.rrd.monolith.test.assertRight
import uk.co.ceilingcat.rrd.monolith.test.createTempDirectory
import uk.co.ceilingcat.rrd.monolith.test.set
import java.util.Properties
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainFunctionalSpec {

    @Test
    fun `That if all input gateways return errors, we should obtain a MainExecutionError`() {
        withRefuseDaysHence(0).let { spreadsheetDirectory ->
            MainExecutionError assertLeft mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME to
                            AllInputGatewaysFail::class.qualifiedName!!
                    )
                )
            }).flatMap { (bootstrapProperties) -> Main(bootstrapProperties).execute }
        }
    }

    @Test
    fun `That if all output gateways return errors, we should obtain a MainExecutionError`() {
        withRefuseDaysHence(1).let { spreadsheetDirectory ->
            MainExecutionError assertLeft mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to
                            spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME to
                            AllOutputGatewaysFail::class.qualifiedName!!
                    )
                )
            }).flatMap { (bootstrapProperties) -> Main(bootstrapProperties).execute }
        }
    }

    @Test
    fun `That if the factory returns no input gateways, we get a MainFactoryError`() {
        withRefuseDaysHence(1).let { spreadsheetDirectory ->
            MainFactoryError assertLeft mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to
                            spreadsheetDirectory.canonicalPath,
                        APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME to
                            NoInputGateways::class.qualifiedName!!
                    )
                )
            }).flatMap { (bootstrapProperties) -> Main(bootstrapProperties).execute }
        }
    }

    @Test
    fun `That if the factory returns no output gateways, we get a MainFactoryError`() {
        withRefuseDaysHence(1).let { spreadsheetDirectory ->
            MainFactoryError assertLeft mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to
                            spreadsheetDirectory.canonicalPath,
                        APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME to
                            NoOutputGateways::class.qualifiedName!!
                    )
                )
            }).flatMap { (bootstrapProperties) -> Main(bootstrapProperties).execute }
        }
    }

    @Test
    fun `If the factory factory class cannot be constructed, we get a MainFactoryError`() {
        MainFactoryError assertLeft mutateProperties({ properties ->
            properties.set(
                mapOf(
                    APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME to "you.cannot.construct.this"
                )
            )
        }).flatMap { (bootstrapProperties) -> Main(bootstrapProperties).execute }
    }

    @Test
    fun `That if no upcoming events are found, we neither send an email nor, create a calendar event`() {
        val nonce = UUID.randomUUID()
        val summaryAndSubjectTemplate = "$nonce - <<serviceType>>"
        withEmpty().let { spreadsheetDirectory ->
            true assertRight mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    right(
                        applicationPropertiesAndLocation.let { (it, _) ->
                            !foundAndRemovedMail(it, "$nonce - Refuse") && !foundEvent(it, summaryAndSubjectTemplate)
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `If we can't parse the service type and we're the only input gateway, we yield a MainExecution Error and no notifications`() {
        val nonce = UUID.randomUUID()
        val summaryAndSubjectTemplate = "$nonce - <<serviceType>>"
        withSheet(
            HISTORIC,
            listOf(
                CellData(
                    CellAddress(
                        SheetAddress("Friday Wk2"),
                        "B4"
                    ),
                    "not a service type date"
                )
            )
        ).let { spreadsheetDirectory ->
            true assertRight mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    right(
                        applicationPropertiesAndLocation.let { (it, _) ->
                            !foundAndRemovedMail(it, "$nonce - Refuse") && !foundEvent(it, summaryAndSubjectTemplate)
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `That if no upcoming events are found, as we have our street but no date header, we neither send an email nor, create a calendar event`() {
        val nonce = UUID.randomUUID()
        val summaryAndSubjectTemplate = "$nonce - <<serviceType>>"
        withSheet(
            HISTORIC,
            listOf(
                CellData(
                    CellAddress(
                        SheetAddress("Friday Wk2"),
                        "B3"
                    ),
                    "not a date"
                )
            )
        ).let { spreadsheetDirectory ->
            true assertRight mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    right(
                        applicationPropertiesAndLocation.let { (it, _) ->
                            !foundAndRemovedMail(it, "$nonce - Refuse") && !foundEvent(it, summaryAndSubjectTemplate)
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `That if there are no events for our chosen street, we neither send an email nor, create a calendar event`() {
        val nonce = UUID.randomUUID()
        val summaryAndSubjectTemplate = "$nonce - <<serviceType>>"
        true assertRight withRefuseDaysHence().let { spreadsheetDirectory ->
            mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        STREET_NAME_PROPERTY_NAME to nonce.toString(),
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to summaryAndSubjectTemplate,
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    applicationPropertiesAndLocation.let { (it, _) ->
                        right(!foundAndRemovedMail(it, "$nonce - Refuse") && !foundEvent(it, summaryAndSubjectTemplate))
                    }
                }
            }
        }
    }

    @Test
    fun `That we don't raise errors on malformed input spreadsheets`() {
        Unit assertRight worksheetFile(MALFORMED).parentFile.let { spreadsheetDirectory ->
            mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap { (bootstrapProperties, _) ->
                Main((bootstrapProperties)).execute
            }
        }
    }

    @Test
    fun `That we process empty spreadsheets without error`() {
        Unit assertRight withSheet(EMPTY).parentFile.let { spreadsheetDirectory ->
            mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap { (bootstrapProperties, _) ->
                Main((bootstrapProperties)).execute
            }
        }
    }

    @Test
    fun `That we process empty spreadsheet directories without error`() {
        Unit assertRight createTempDirectory().let { spreadsheetDirectory ->
            mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap { (bootstrapProperties, _) ->
                Main((bootstrapProperties)).execute
            }
        }
    }

    @Test
    fun `That the next upcoming event sourced from xlsx is delivered by email and, as a calendar event`() {
        val nonce = UUID.randomUUID()
        withRefuseDaysHence().let { spreadsheetDirectory ->
            true assertRight mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    right(
                        applicationPropertiesAndLocation.let { (it, _) ->
                            foundAndRemovedMail(it, "$nonce - Refuse") && foundAndRemovedEvent(it, "$nonce - Refuse")
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `Events too distant in time are not notified about`() {
        val nonce = UUID.randomUUID()
        withRefuseDaysHence(DAY_HENCE_NOTIFIED_OF + DAY_HENCE_NOTIFIED_OF).let { spreadsheetDirectory ->
            false assertRight mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetDirectory.canonicalPath,
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        SUBJECT_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                        CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                    )
                )
            }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
                Main((bootstrapProperties)).execute.flatMap { _ ->
                    right(
                        applicationPropertiesAndLocation.let { (it, _) ->
                            foundAndRemovedMail(it, "$nonce - Refuse") || foundAndRemovedEvent(it, "$nonce - Refuse")
                        }
                    )
                }
            }
        }
    }

    @Test
    fun `If two events are found in different spreadsheets, only the one that occurs first is delivered`() {
        val nonce = UUID.randomUUID()
        val spreadSheetDirectory = withRefuseDaysHence(DAY_HENCE_NOTIFIED_OF)
        withRefuseDaysHence(DAY_HENCE_NOTIFIED_OF + DAY_HENCE_NOTIFIED_OF, spreadSheetDirectory)

        true assertRight mutateProperties({ properties ->
            properties.set(
                mapOf(
                    WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadSheetDirectory.canonicalPath,
                    INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                    SUBJECT_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                    CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME to "$nonce - <<serviceType>>",
                )
            )
        }).flatMap { (bootstrapProperties, applicationPropertiesAndLocation) ->
            Main((bootstrapProperties)).execute.flatMap { _ ->
                right(
                    applicationPropertiesAndLocation.let { (it, _) ->
                        foundAndRemovedMail(it, "$nonce - Refuse") &&
                            foundAndRemovedEvent(it, "$nonce - Refuse")
                    }
                )
            }
        }
    }

    private fun foundAndRemovedMail(it: Properties, subjectText: String) = mailFoundAndRemoved(
        it.getProperty(EMAIL_USER_NAME_PROPERTY_NAME),
        it.getProperty(EMAIL_PASSWORD_PROPERTY_NAME),
        subjectText,
        MAIL_CHECK_TIMEOUT_MS,
        MAIL_CHECK_WAIT_MS
    )

    private fun foundEvent(it: Properties, summaryText: String) =
        eventFoundWithSummary(
            CalendarName(it.getProperty(CALENDAR_NAME_PROPERTY_NAME)),
            summaryText
        )

    private fun foundAndRemovedEvent(it: Properties, summaryText: String) =
        eventFoundWithSummaryAndRemoved(
            CalendarName(it.getProperty(CALENDAR_NAME_PROPERTY_NAME)),
            summaryText
        )
}
