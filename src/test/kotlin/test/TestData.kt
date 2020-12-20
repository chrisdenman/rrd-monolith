package uk.co.ceilingcat.rrd.monolith.test

import java.io.File
import java.util.concurrent.TimeUnit

class TestData {
    companion object {
        const val PROPERTIES_PATH_PROPERTY_NAME = "PROPERTIES_PATH"
        const val APP_TEST_PROPERTIES_PATH = "/etc/refuseRecyclingDates/test"

        const val EXTENSION_XLSX = "xlsx"

        const val APPLICATION_FACTORY_FACTORY_CLASS_NAME_PROPERTY_NAME = "APPLICATION_FACTORY_FACTORY_CLASS_NAME"
        const val CALENDAR_NAME_PROPERTY_NAME = "CALENDAR_NAME"
        const val CALENDAR_SUMMARY_TEMPLATE_PROPERTY_NAME = "CALENDAR_SUMMARY_TEMPLATE"
        const val EMAIL_BODY_TEXT_PROPERTY_NAME = "EMAIL_BODY_TEXT"
        const val EMAIL_FROM_PROPERTY_NAME = "EMAIL_FROM"
        const val EMAIL_PASSWORD_PROPERTY_NAME = "EMAIL_PASSWORD"
        const val EMAIL_TO_PROPERTY_NAME = "EMAIL_TO"
        const val EMAIL_USER_NAME_PROPERTY_NAME = "EMAIL_USER_NAME"
        const val INPUT_GATEWAY_PATTERN_PROPERTY_NAME = "INPUT_GATEWAY_PATTERN"
        const val MAXIMUM_NOTIFY_DURATION_SECONDS_PROPERTY_NAME = "MAXIMUM_NOTIFY_DURATION_SECONDS"
        const val OUTPUT_GATEWAY_PATTERN_PROPERTY_NAME = "OUTPUT_GATEWAY_PATTERN"
        const val STREET_NAME_PROPERTY_NAME = "STREET_NAME"
        const val SUBJECT_TEMPLATE_PROPERTY_NAME = "SUBJECT_TEMPLATE"
        const val WORKSHEETS_SEARCH_DIRECTORY_PROPERTY_NAME = "WORKSHEETS_SEARCH_DIRECTORY"

        const val EMAIL_OUTPUT_GATEWAY_PROPERTY_VALUE = "EMailOutputGateway"
        const val CALENDAR_OUTPUT_GATEWAY_PROPERTY_VALUE = "CalendarOutputGateway"

        val APPLICATION_FACTORY_FACTORY_CLASS_NAME_LENGTH_RANGE = 1..300
        val CALENDAR_NAME_LENGTH_RANGE = 1..100
        val CALENDAR_SUMMARY_TEMPLATE_LENGTH_RANGE = 1..100
        val EMAIL_PASSWORD_LENGTH_RANGE = 8..100
        val EMAIL_USER_NAME_LENGTH_RANGE = 8..50
        val GATEWAY_PATTERN_LENGTH_RANGE = 1..1024
        val STREET_NAME_LENGTH_RANGE = 4..50
        val SUBJECT_TEMPLATE_LENGTH_RANGE = 10..100

        val MAXIMUM_NOTIFY_DURATION_SECONDS_BOUNDS_RANGE = 1..60

        const val EXPECTED_XLSX_DATE_FORMAT = "d MMMM"

        val MAIL_CHECK_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5L)

        val MAIL_CHECK_WAIT_MS = TimeUnit.MILLISECONDS.toMillis(500L)

        const val EMAIL_BODY_TEXT_MAX_LENGTH = 200

        const val DAY_HENCE_NOTIFIED_OF = 1L

        private val WORKSHEETS_DIRECTORY = File("src/test/resources/worksheets")

        val worksheetFile: (worksheet: WORKSHEETS) -> File =
            { File(WORKSHEETS_DIRECTORY, "${it.filenameWithoutExtension}/${it.filenameWithoutExtension}.$EXTENSION_XLSX") }

        enum class WORKSHEETS(val filenameWithoutExtension: String) {
            HISTORIC("historic"),
            MALFORMED("malformed"),
            EMPTY("empty"),
            REAL("real")
        }
    }
}
