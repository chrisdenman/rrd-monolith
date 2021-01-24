package uk.co.ceilingcat.rrd.monolith

import arrow.core.compose
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.CalendarName
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.CalendarSummaryTemplate
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.MaximumNotifyDurationSeconds
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.EmailBodyText
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.EmailFrom
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.EmailPassword
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.EmailTo
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.EmailUserName
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.SubjectTemplate
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.DriverLocation
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.DriverOptions
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.DriverProperty
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.PostCodeSearchTerm
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.StartUrl
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.StreetNameSearchTerm
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.WaitDurationSeconds
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.StreetName
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.WorkSheetsSearchDirectory
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.APPLICATION_FACTORY_FACTORY_CLASS_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.CALENDAR_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.CALENDAR_SUMMARY_TEMPLATE
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.DRIVER_LOCATION
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.DRIVER_OPTIONS
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.DRIVER_PROPERTY
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_BODY_TEXT
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_FROM
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_PASSWORD
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_TO
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_USER_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.INPUT_GATEWAY_PATTERN
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.MAXIMUM_NOTIFY_DURATION_SECONDS
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.OUTPUT_GATEWAY_PATTERN
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.POST_CODE_SEARCH_TERM
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.START_URL
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.STREET_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.STREET_NAME_SEARCH_TERM
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.SUBJECT_TEMPLATE
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.WAIT_DURATION_SECONDS
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.WORKSHEETS_SEARCH_DIRECTORY
import java.io.File
import java.net.URL
import java.util.regex.Pattern
import javax.mail.internet.InternetAddress

enum class ConfigurationProperties(val propertyName: PropertyName) {
    APPLICATION_FACTORY_FACTORY_CLASS_NAME("APPLICATION_FACTORY_FACTORY_CLASS_NAME"),
    CALENDAR_NAME("CALENDAR_NAME"),
    CALENDAR_SUMMARY_TEMPLATE("CALENDAR_SUMMARY_TEMPLATE"),
    DRIVER_PROPERTY("DRIVER_PROPERTY"),
    DRIVER_LOCATION("DRIVER_LOCATION"),
    DRIVER_OPTIONS("DRIVER_OPTIONS"),
    EMAIL_BODY_TEXT("EMAIL_BODY_TEXT"),
    EMAIL_FROM("EMAIL_FROM"),
    EMAIL_PASSWORD("EMAIL_PASSWORD"),
    EMAIL_TO("EMAIL_TO"),
    EMAIL_USER_NAME("EMAIL_USER_NAME"),
    INPUT_GATEWAY_PATTERN("INPUT_GATEWAY_PATTERN"),
    MAXIMUM_NOTIFY_DURATION_SECONDS("MAXIMUM_NOTIFY_DURATION_SECONDS"),
    OUTPUT_GATEWAY_PATTERN("OUTPUT_GATEWAY_PATTERN"),
    POST_CODE_SEARCH_TERM("POST_CODE_SEARCH_TERM"),
    START_URL("START_URL"),
    STREET_NAME("STREET_NAME"),
    STREET_NAME_SEARCH_TERM("STREET_NAME_SEARCH_TERM"),
    SUBJECT_TEMPLATE("SUBJECT_TEMPLATE"),
    WAIT_DURATION_SECONDS("WAIT_DURATION_SECONDS"),
    WORKSHEETS_SEARCH_DIRECTORY("WORKSHEETS_SEARCH_DIRECTORY")
}

// @tod factor all the stuff below
val applicationFactoryFactoryClassNameConfiguration: (PropertySource) -> PropertyConfiguration<ApplicationFactoryFactoryClassName> =
    {
        APPLICATION_FACTORY_FACTORY_CLASS_NAME.propertyName.let { propertyName ->
            PropertyConfiguration(
                propertyName,
                propertySourceFetcher(it, propertyName),
                isFqcn compose lengthIn(1, 300),
                { propertyValue -> ApplicationFactoryFactoryClassName(propertyValue) }
            )
        }
    }

val calendarNameConfiguration: (PropertySource) -> PropertyConfiguration<CalendarName> = {
    CALENDAR_NAME.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(1, 100),
            { propertyValue -> CalendarName(propertyValue) }
        )
    }
}

val calendarSummaryTemplateConfiguration: (PropertySource) -> PropertyConfiguration<CalendarSummaryTemplate> = {
    CALENDAR_SUMMARY_TEMPLATE.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(1, 100),
            { propertyValue -> CalendarSummaryTemplate(propertyValue) }
        )
    }
}

val driverPropertyConfiguration: (PropertySource) -> PropertyConfiguration<DriverProperty> = {
    DRIVER_PROPERTY.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose lengthIn(1, 200),
            { propertyValue -> DriverProperty(propertyValue) }
        )
    }
}

val driverLocationConfiguration: (PropertySource) -> PropertyConfiguration<DriverLocation> = {
    DRIVER_LOCATION.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            fileExists,
            { propertyValue -> DriverLocation(File(propertyValue)) }
        )
    }
}

val driverOptionsConfiguration: (PropertySource) -> PropertyConfiguration<DriverOptions> = {
    DRIVER_OPTIONS.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose lengthIn(0, 1000),
            { propertyValue -> DriverOptions(propertyValue) }
        )
    }
}

val emailBodyTextConfiguration: (PropertySource) -> PropertyConfiguration<EmailBodyText> = {
    EMAIL_BODY_TEXT.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose lengthIn(0, 200),
            { propertyValue -> EmailBodyText(propertyValue) }
        )
    }
}

val emailFromConfiguration: (PropertySource) -> PropertyConfiguration<EmailFrom> = {
    EMAIL_FROM.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            isEmail,
            { propertyValue -> EmailFrom(InternetAddress(propertyValue, true)) }
        )
    }
}

val emailPasswordConfiguration: (PropertySource) -> PropertyConfiguration<EmailPassword> = {
    EMAIL_PASSWORD.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(8, 100),
            { propertyValue -> EmailPassword(propertyValue) }
        )
    }
}

val emailToConfiguration: (PropertySource) -> PropertyConfiguration<EmailTo> = {
    EMAIL_TO.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            isEmail,
            { propertyValue -> EmailTo(InternetAddress(propertyValue, true)) }
        )
    }
}

val emailUserNameConfiguration: (PropertySource) -> PropertyConfiguration<EmailUserName> = {
    EMAIL_USER_NAME.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(8, 50),
            { propertyValue -> EmailUserName(propertyValue) }
        )
    }
}

val inputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<InputGatewayPattern> = {
    INPUT_GATEWAY_PATTERN.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
            { propertyValue -> InputGatewayPattern(Pattern.compile(propertyValue)) }
        )
    }
}

val MaximumNotifyDurationSeconds: (PropertySource) -> PropertyConfiguration<MaximumNotifyDurationSeconds> = {
    MAXIMUM_NOTIFY_DURATION_SECONDS.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            inClosedInterval(1, 60),
            { propertyValue -> MaximumNotifyDurationSeconds(propertyValue.toLong()) }
        )
    }
}

val outputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<OutputGatewayPattern> = {
    OUTPUT_GATEWAY_PATTERN.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
            { propertyValue -> OutputGatewayPattern(Pattern.compile(propertyValue)) }
        )
    }
}

val streetNameConfiguration: (PropertySource) -> PropertyConfiguration<StreetName> = {
    STREET_NAME.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(4, 50),
            { propertyValue -> StreetName(propertyValue) }
        )
    }
}

val subjectTemplateConfiguration: (PropertySource) -> PropertyConfiguration<SubjectTemplate> = {
    SUBJECT_TEMPLATE.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(10, 100),
            { propertyValue -> SubjectTemplate(propertyValue) }
        )
    }
}

val worksheetsSearchDirectoryConfiguration: (PropertySource) -> PropertyConfiguration<WorkSheetsSearchDirectory> = {
    WORKSHEETS_SEARCH_DIRECTORY.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            fileExists compose isDirectory,
            { propertyValue -> WorkSheetsSearchDirectory(File(propertyValue)) }
        )
    }
}

val streetNameSearchTermConfiguration: (PropertySource) -> PropertyConfiguration<StreetNameSearchTerm> = {
    STREET_NAME_SEARCH_TERM.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(5, 50),
            { propertyValue -> StreetNameSearchTerm(propertyValue) }
        )
    }
}

val postCodeSearchTermConfiguration: (PropertySource) -> PropertyConfiguration<PostCodeSearchTerm> = {
    POST_CODE_SEARCH_TERM.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            notBlank compose noCrLfsTabs compose lengthIn(5, 50),
            { propertyValue -> PostCodeSearchTerm(propertyValue) }
        )
    }
}

val startUrlConfiguration: (PropertySource) -> PropertyConfiguration<StartUrl> = {
    START_URL.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            isUrl,
            { propertyValue -> StartUrl(URL(propertyValue)) }
        )
    }
}

val waitDurationSecondsConfiguration: (PropertySource) -> PropertyConfiguration<WaitDurationSeconds> = {
    WAIT_DURATION_SECONDS.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(it, propertyName),
            inClosedInterval(1, Long.MAX_VALUE),
            { propertyValue -> WaitDurationSeconds(propertyValue.toLong()) }
        )
    }
}
