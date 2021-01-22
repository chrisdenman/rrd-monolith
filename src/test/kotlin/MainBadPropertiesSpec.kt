package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either.Companion.left
import arrow.core.flatMap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.co.ceilingcat.rrd.monolith.ConfigurationException.CouldNotCreateConfigurationException
import uk.co.ceilingcat.rrd.monolith.PropertyException.CouldNotCreatePropertyException
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.PropertiesAndLocation
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.loadProperties
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.removeProperty
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.saveProperties
import uk.co.ceilingcat.rrd.monolith.test.PropertiesHelper.Companion.setProperty
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APPLICATION_FACTORY_FACTORY_CLASS_NAME_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APP_TEST_PROPERTIES_PATH
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_NAME_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_SUMMARY_TEMPLATE_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DRIVER_LOCATION_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DRIVER_OPTIONS_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DRIVER_OPTIONS_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DRIVER_PROPERTY_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.DRIVER_PROPERTY_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_BODY_TEXT_MAX_LENGTH
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_BODY_TEXT_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_FROM_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_OUTPUT_GATEWAY_PROPERTY_VALUE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_PASSWORD_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_PASSWORD_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_TO_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_USER_NAME_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.EMAIL_USER_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.GATEWAY_PATTERN_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.INPUT_GATEWAY_PATTERN_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.MAXIMUM_NOTIFY_DURATION_SECONDS_BOUNDS_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.MAXIMUM_NOTIFY_DURATION_SECONDS_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.POST_CODE_SEARCH_TERM_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.POST_CODE_SEARCH_TERM_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.PROPERTIES_PATH_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.START_URL_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.STREET_NAME_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.STREET_NAME_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.STREET_NAME_SEARCH_TERM_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.SUBJECT_TEMPLATE_LENGTH_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.SUBJECT_TEMPLATE_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WAIT_DURATION_SECONDS_BOUNDS_RANGE
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WAIT_DURATION_SECONDS_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.XLSX_INPUT_GATEWAY_PROPERTY_VALUE
import uk.co.ceilingcat.rrd.monolith.test.XlsxHelper.Companion.withRefuseDaysHence
import uk.co.ceilingcat.rrd.monolith.test.assertEquals
import uk.co.ceilingcat.rrd.monolith.test.assertLeft
import uk.co.ceilingcat.rrd.monolith.test.nonExistentDirectory
import uk.co.ceilingcat.rrd.monolith.test.set
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MainBadPropertiesSpec {

    @Test
    fun `Executing main without defining the 'PROPERTIES_PATH' property, yields NoPropertyPathPropertyError`() {
        assert(Main(Properties()).execute == left(NoPropertyPathPropertyDefined))
    }

    @Test
    fun `Executing main and pointing the 'PROPERTIES_PATH' property to a missing file, yields NoPropertiesFileError`() {
        UUID.randomUUID().toString().let { propertiesPath ->
            assertFailureWithPropertyPathPropertyValue(
                propertiesPath,
                PropertiesFileDoesNotExist("The properties file path '$propertiesPath' does not exist.")
            )
        }
    }

    @Test
    fun `Executing main and pointing the 'PROPERTIES_PATH' property to a directory, yields PropertiesFileIsDirectoryError`() {
        System.getProperty("user.home").let { propertiesPath ->
            assertFailureWithPropertyPathPropertyValue(
                propertiesPath,
                PropertiesFileIsDirectory(
                    "The properties file path " +
                        "'${File(propertiesPath).absolutePath}' is a directory."
                )
            )
        }
    }

    @Test
    fun `Executing main and pointing the 'PROPERTIES_PATH' property to a file we do not have permissions to read, yields a MainPropertiesFileLoadError`() {
        "/etc/ssh/ssh_host_rsa_key".let { propertiesPath ->
            assertFailureWithPropertyPathPropertyValue(
                propertiesPath,
                UnableToLoadPropertiesFile(
                    "Could not load the properties file '${File(propertiesPath).absolutePath}'.",
                    FileNotFoundException("$propertiesPath (Permission denied)")
                )
            )
        }
    }

    @Test
    fun `Executing main and pointing the 'PROPERTIES_PATH' to an malformed properties file, yields a MalformedPropertiesFileError`() {
        File("./src/test/resources/properties/malformed.properties_").absolutePath.let { propertiesPath ->
            assertFailureWithPropertyPathPropertyValue(
                propertiesPath,
                MalformedPropertiesFile(
                    "The properties file '$propertiesPath' is malformed.",
                    IllegalArgumentException("Malformed \\uxxxx encoding.")
                )
            )
        }
    }

    @Test
    fun `Executing main and referencing a properties file that does not contain all required properties yields a main configuration error`() {
        listOf(
            APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME,
            CALENDAR_NAME_PROPERTY_NAME,
            CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME,
            EMAIL_FROM_PROPERTY_NAME,
            EMAIL_PASSWORD_PROPERTY_NAME,
            EMAIL_TO_PROPERTY_NAME,
            EMAIL_USER_NAME_PROPERTY_NAME,
            INPUT_GATEWAY_PATTERN_PROPERTY_NAME,
            STREET_NAME_PROPERTY_NAME,
            MAXIMUM_NOTIFY_DURATION_SECONDS_PROPERTY_NAME,
            EMAIL_BODY_TEXT_PROPERTY_NAME,
            SUBJECT_TEMPLATE_PROPERTY_NAME,
            WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME,
            OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME
        ).forEach { propertyName ->
            CouldNotConstructApplicationConfiguration(
                CouldNotCreateConfigurationException(
                    CouldNotCreatePropertyException(
                        "Could not validate and construct the '$propertyName' property.",
                        NoSuchProperty("There is no property with name '$propertyName'.")
                    )
                )
            ) assertLeft executeMainWithoutProperty(propertyName)
        }
    }

    @Test
    fun `Executing main and setting the 'APPLICATION_FACTORY_FACTORY_CLASS_NAME' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME, APPLICATION_FACTORY_FACTORY_CLASS_NAME_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'APPLICATION_FACTORY_FACTORY_CLASS_NAME' property to a no fully-qualified class name, yields a main configuration error`() {
        APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME.let { propertyName ->
            NON_FQCNS.forEach { propertyValue ->
                propertyValidationFailedException(propertyName, propertyValue) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_NAME' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(CALENDAR_NAME_PROPERTY_NAME, CALENDAR_NAME_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_NAME' property to a blank value, yields a main configuration error`() {
        testBlankProperty(CALENDAR_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_NAME' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(CALENDAR_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_SUMMARY_TEMPLATE' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME, CALENDAR_SUMMARY_TEMPLATE_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_SUMMARY_TEMPLATE' property to a blank value, yields a main configuration error`() {
        testBlankProperty(CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'CALENDAR_SUMMARY_TEMPLATE' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_BODY_TEXT' property to a blank value yields a main configuration error`() {
        testBlankProperty(EMAIL_BODY_TEXT_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_BODY_TEXT' property to too long a value yields a main configuration error`() {
        EMAIL_BODY_TEXT_PROPERTY_NAME.let { propertyName ->
            EMPTY_TEXT.padStart(EMAIL_BODY_TEXT_MAX_LENGTH + 1, 'x').let { propertyValue ->
                propertyValidationFailedException(propertyName, propertyValue) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }
    }

    @Test
    fun `Executing main and setting the 'EMAIL_FROM' property to an invalid email address, yields a main configuration error`() {
        EMAIL_FROM_PROPERTY_NAME.let { propertyName ->
            NON_EMAILS.forEach { propertyValue ->
                propertyValidationThrowsException(propertyName) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }
    }

    @Test
    fun `Executing main and setting the 'EMAIL_PASSWORD' property to a blank value, yields a main configuration error`() {
        testBlankProperty(EMAIL_PASSWORD_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_PASSWORD' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(EMAIL_PASSWORD_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_PASSWORD' property to a value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(EMAIL_PASSWORD_PROPERTY_NAME, EMAIL_PASSWORD_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_TO' property to an invalid email address, yields a main configuration error`() {
        EMAIL_TO_PROPERTY_NAME.let { propertyName ->
            NON_EMAILS.forEach { propertyValue ->
                propertyValidationThrowsException(propertyName) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }
    }

    @Test
    fun `Executing main and setting the 'EMAIL_USER_NAME' property to a value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(EMAIL_USER_NAME_PROPERTY_NAME, EMAIL_USER_NAME_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_USER_NAME' property to a blank value, yields a main configuration error`() {
        testBlankProperty(EMAIL_USER_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'EMAIL_USER_NAME' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(EMAIL_USER_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'POST_CODE_SEARCH_TERM' property to a blank value, yields a main configuration error`() {
        testBlankProperty(POST_CODE_SEARCH_TERM_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'POST_CODE_SEARCH_TERM' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(POST_CODE_SEARCH_TERM_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'POST_CODE_SEARCH_TERM' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(POST_CODE_SEARCH_TERM_PROPERTY_NAME, POST_CODE_SEARCH_TERM_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'START_URL' property to an invalid url, yields a main configuration error`() {
        START_URL_PROPERTY_NAME.let { propertyName ->
            "\\//\\//".let { propertyValue ->
                propertyValidationThrowsException(propertyName) assertLeft
                    executeMainWithPropertyValue(propertyName, "\\//\\//")
            }
        }
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME_SEARCH_TERM' property to a blank value, yields a main configuration error`() {
        testBlankProperty(STREET_NAME_SEARCH_TERM_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME_SEARCH_TERM' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(STREET_NAME_SEARCH_TERM_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME_SEARCH_TERM' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(STREET_NAME_SEARCH_TERM_PROPERTY_NAME, POST_CODE_SEARCH_TERM_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'WAIT_DURATION_SECONDS' property to a non integral value, yields a main configuration error`() {
        testNonIntegralsProperty(WAIT_DURATION_SECONDS_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'WAIT_DURATION_SECONDS' property to a value out of bounds, yields a main configuration error`() {
        WAIT_DURATION_SECONDS_PROPERTY_NAME.let { propertyName ->
            WAIT_DURATION_SECONDS_BOUNDS_RANGE.let { range ->
                (range.first - 1).toString().let { propertyValue ->
                    propertyValidationFailedException(propertyName, propertyValue) assertLeft
                        executeMainWithPropertyValue(propertyName, propertyValue)
                }
                (range.last + 1).toString().let { propertyValue ->
                    propertyValidationFailedException(propertyName, propertyValue) assertLeft
                        executeMainWithPropertyValue(propertyName, propertyValue)
                }
            }
        }
    }

    @Test
    fun `Executing main and setting the 'DRIVER_PROPERTY' property to a blank value, yields a main configuration error`() {
        testBlankProperty(DRIVER_PROPERTY_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'DRIVER_PROPERTY' property to a value out of bounds, yields a main configuration error`() {
        testRangeStringProperty(DRIVER_PROPERTY_PROPERTY_NAME, DRIVER_PROPERTY_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'DRIVER_LOCATION' property to a non extant file, yields a main configuration error`() {
        testNonExtantFileProperty(DRIVER_LOCATION_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'DRIVER_OPTIONS' property to a blank value, yields a main configuration error`() {
        testBlankProperty(DRIVER_OPTIONS_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'DRIVER_OPTIONS' property to a value out of bounds, yields a main configuration error`() {
        testRangeStringProperty(DRIVER_OPTIONS_PROPERTY_NAME, DRIVER_OPTIONS_LENGTH_RANGE)
    }

    @Test
    fun `An incorrect email password, results in a MainExecutionError`() {
        MainExecutionError assertLeft withRefuseDaysHence().let { spreadsheetFile ->
            PropertiesHelper.mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetFile.canonicalPath,
                        EMAIL_PASSWORD_PROPERTY_NAME to "this_is_wrong",
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME to EMAIL_OUTPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap {
                Main(it.bootstrapProperties).execute
            }
        }
    }

    @Test
    fun `An incorrect email username, results in a MainExecutionError`() {
        MainExecutionError assertLeft withRefuseDaysHence().let { spreadsheetFile ->
            PropertiesHelper.mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetFile.canonicalPath,
                        EMAIL_USER_NAME_PROPERTY_NAME to "this_is_wrong",
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME to EMAIL_OUTPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap {
                Main(it.bootstrapProperties).execute
            }
        }
    }

    @Test
    fun `An incorrect email source mailbox, results in a MainExecutionError`() {
        MainExecutionError assertLeft withRefuseDaysHence().let { spreadsheetFile ->
            PropertiesHelper.mutateProperties({ properties ->
                properties.set(
                    mapOf(
                        WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME to spreadsheetFile.canonicalPath,
                        EMAIL_USER_NAME_PROPERTY_NAME to "this_is_wrong@afasdfas",
                        INPUT_GATEWAY_PATTERN_PROPERTY_NAME to XLSX_INPUT_GATEWAY_PROPERTY_VALUE,
                        OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME to EMAIL_OUTPUT_GATEWAY_PROPERTY_VALUE
                    )
                )
            }).flatMap {
                Main(it.bootstrapProperties).execute
            }
        }
    }

    @Test
    fun `Executing main and setting the 'INPUT_GATEWAY_PATTERN' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(INPUT_GATEWAY_PATTERN_PROPERTY_NAME, GATEWAY_PATTERN_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'INPUT_GATEWAY_PATTERN' property to a blank value, yields a main configuration error`() {
        testBlankProperty(INPUT_GATEWAY_PATTERN_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'INPUT_GATEWAY_PATTERN' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(INPUT_GATEWAY_PATTERN_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'MAXIMUM_NOTIFY_DURATION_SECONDS' property to a non integral value, yields a main configuration error`() {
        testNonIntegralsProperty(MAXIMUM_NOTIFY_DURATION_SECONDS_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'MAXIMUM_NOTIFY_DURATION_SECONDS' property to a value out of bounds, yields a main configuration error`() {
        MAXIMUM_NOTIFY_DURATION_SECONDS_PROPERTY_NAME.let { propertyName ->
            MAXIMUM_NOTIFY_DURATION_SECONDS_BOUNDS_RANGE.let { range ->
                (range.first - 1).toString().let { propertyValue ->
                    propertyValidationFailedException(propertyName, propertyValue) assertLeft
                        executeMainWithPropertyValue(propertyName, propertyValue)
                }
                (range.last + 1).toString().let { propertyValue ->
                    propertyValidationFailedException(propertyName, propertyValue) assertLeft
                        executeMainWithPropertyValue(propertyName, propertyValue)
                }
            }
        }
    }

    @Test
    fun `Executing main and setting the 'OUTPUT_GATEWAY_PATTERN' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME, GATEWAY_PATTERN_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'OUTPUT_GATEWAY_PATTERN' property to a blank value, yields a main configuration error`() {
        testBlankProperty(OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'OUTPUT_GATEWAY_PATTERN' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(STREET_NAME_PROPERTY_NAME, STREET_NAME_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME' property to a blank value, yields a main configuration error`() {
        testBlankProperty(STREET_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'STREET_NAME' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(STREET_NAME_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'SUBJECT_TEMPLATE' property to value out of length bounds, yields a main configuration error`() {
        testRangeStringProperty(SUBJECT_TEMPLATE_PROPERTY_NAME, SUBJECT_TEMPLATE_LENGTH_RANGE)
    }

    @Test
    fun `Executing main and setting the 'SUBJECT_TEMPLATE' property to a blank value, yields a main configuration error`() {
        testBlankProperty(SUBJECT_TEMPLATE_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'SUBJECT_TEMPLATE' property to a value containing CRs or LFs or TABs, yields a main configuration error`() {
        testCrLfTabsProperty(SUBJECT_TEMPLATE_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'WORKSHEETS_SEARCH_DIRECTORY' property to a non extant directory, yields a main configuration error`() {
        testNonExtantFileProperty(WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME)
    }

    @Test
    fun `Executing main and setting the 'INPUT_GATEWAY_PATTERN' property to a value that precludes all input gateways, yields an error`() {
        left(MainFactoryError) assertEquals executeMainWithPropertyValue(INPUT_GATEWAY_PATTERN_PROPERTY_NAME, ".")
    }

    @Test
    fun `Executing main and setting the 'OUTPUT_GATEWAY_PATTERN' property to a value that precludes all input gateways, yields an error`() {
        left(MainFactoryError) assertEquals executeMainWithPropertyValue(OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME, ".")
    }

    companion object {
        private val NON_INTEGRALS = listOf("", " ", "1.2", "A", "1.2f", "2d")
        private val BLANKS = listOf(" ", "   ")
        private val NON_EMAILS = BLANKS + listOf("@", "@@", "hello", "hello@", "@hello.com")
        private val CRLFSTABS = listOf("\r", "\n", "\t")
        private val NON_FQCNS = listOf(" ", ".", "HELLO..")

        private const val EMPTY_TEXT = ""

        private fun propertyValidationFailedException(propertyName: PropertyName, propertyValueText: String, cause: Throwable? = null) =
            CouldNotConstructApplicationConfiguration(
                CouldNotCreateConfigurationException(
                    CouldNotCreatePropertyException(
                        "Could not validate and construct the '$propertyName' property.",
                        PropertyFailedValidation("The property value '$propertyValueText' failed validation.", cause)
                    )
                )
            )

        private fun propertyValidationThrowsException(propertyName: PropertyName) =
            CouldNotConstructApplicationConfiguration(
                CouldNotCreateConfigurationException(
                    CouldNotCreatePropertyException(
                        "An exception was caught whilst validating and constructing the '$propertyName' property."
                    )
                )
            )

        private fun testPropertyOver(propertyName: String, values: List<String>) {
            values.forEach { propertyValue ->
                propertyValidationFailedException(propertyName, propertyValue) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }

        private fun testCrLfTabsProperty(propertyName: String) {
            testPropertyOver(propertyName, CRLFSTABS)
        }

        private fun testNonIntegralsProperty(propertyName: String) {
            NON_INTEGRALS.forEach { propertyValue ->
                propertyValidationThrowsException(propertyName) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }

        private fun testNonExtantFileProperty(propertyName: String) {
            nonExistentDirectory.canonicalPath.let { propertyValue ->
                propertyValidationFailedException(propertyName, propertyValue) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }

        private fun testBlankProperty(propertyName: String) {
            testPropertyOver(propertyName, BLANKS)
        }

        private fun testRangeStringProperty(propertyName: String, range: IntRange) {
            propertyValuesFromRange(range).forEach { propertyValue ->
                propertyValidationFailedException(propertyName, propertyValue) assertLeft
                    executeMainWithPropertyValue(propertyName, propertyValue)
            }
        }

        private fun propertyValuesFromRange(range: IntRange, padChar: Char = 'x'): List<PropertyValue> =
            EMPTY_TEXT.run {
                if (range.first > 0)
                    listOf(
                        padEnd((range.first - 1), padChar),
                        padEnd((range.last + 1), padChar)
                    )
                else
                    listOf(
                        padEnd((range.last + 1), padChar)
                    )
            }

        private fun executeMainWithoutProperty(
            propertyName: PropertyName
        ) = executeMainWithPropertyMutation({ removeProperty(propertyName, it) })

        private fun assertFailureWithPropertyPathPropertyValue(
            propertyValueText: String,
            expectedError: MainRefuseRecyclingDatesError
        ) {
            val result = Main(
                Properties().also {
                    it.setProperty(PROPERTIES_PATH_PROPERTY_NAME, propertyValueText)
                }
            ).execute

            Assertions.assertTrue(result.isLeft())
            result.fold(
                {
                    expectedError assertEquals it
                },
                {}
            )
        }

        private fun executeMainWithPropertyValue(
            propertyName: PropertyName,
            propertyValue: PropertyValue
        ) =
            executeMainWithPropertyMutation({ setProperty(propertyName, propertyValue, it) })

        private fun executeMainWithPropertyMutation(
            mutation: (Properties) -> Unit,
            propertyPath: String = APP_TEST_PROPERTIES_PATH
        ) =
            loadProperties(File(propertyPath))
                .flatMap { properties ->
                    mutation(properties)
                    PropertiesHelper.createTempFile().map { PropertiesAndLocation(properties, it) }
                }
                .flatMap(::saveProperties)
                .flatMap { bootstrapProperties ->
                    Main(
                        Properties().also {
                            it.setProperty(PROPERTIES_PATH_PROPERTY_NAME, bootstrapProperties.location.canonicalPath)
                        }
                    ).execute
                }
    }
}
