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

private fun <T> makePropertyConfiguration(
    property: ConfigurationProperties,
    validator: PropertyValidator,
    constructor: PropertyCreator<T>,
): (PropertySource) -> PropertyConfiguration<T> = { propertySource ->
    property.propertyName.let { propertyName ->
        PropertyConfiguration(
            propertyName,
            propertySourceFetcher(propertySource, propertyName),
            validator,
            constructor
        )
    }
}

val applicationFactoryFactoryClassNameConfiguration: (PropertySource) -> PropertyConfiguration<ApplicationFactoryFactoryClassName> =
    makePropertyConfiguration(
        APPLICATION_FACTORY_FACTORY_CLASS_NAME,
        isFqcn compose lengthIn(1, 300),
        { propertyValue -> ApplicationFactoryFactoryClassName(propertyValue) }
    )

val calendarNameConfiguration: (PropertySource) -> PropertyConfiguration<CalendarName> =
    makePropertyConfiguration(
        CALENDAR_NAME,
        notBlank compose noCrLfsTabs compose lengthIn(1, 100),
        { propertyValue -> CalendarName(propertyValue) }
    )

val calendarSummaryTemplateConfiguration: (PropertySource) -> PropertyConfiguration<CalendarSummaryTemplate> =
    makePropertyConfiguration(
        CALENDAR_SUMMARY_TEMPLATE,
        notBlank compose noCrLfsTabs compose lengthIn(1, 100),
        { propertyValue -> CalendarSummaryTemplate(propertyValue) }
    )

val driverPropertyConfiguration: (PropertySource) -> PropertyConfiguration<DriverProperty> =
    makePropertyConfiguration(
        DRIVER_PROPERTY,
        notBlank compose lengthIn(1, 200),
        { propertyValue -> DriverProperty(propertyValue) }
    )

val driverLocationConfiguration: (PropertySource) -> PropertyConfiguration<DriverLocation> =
    makePropertyConfiguration(
        DRIVER_LOCATION,
        fileExists,
        { propertyValue -> DriverLocation(File(propertyValue)) }
    )

val driverOptionsConfiguration: (PropertySource) -> PropertyConfiguration<DriverOptions> =
    makePropertyConfiguration(
        DRIVER_OPTIONS,
        notBlank compose lengthIn(0, 1000),
        { propertyValue -> DriverOptions(propertyValue) }
    )

val emailBodyTextConfiguration: (PropertySource) -> PropertyConfiguration<EmailBodyText> =
    makePropertyConfiguration(
        EMAIL_BODY_TEXT,
        notBlank compose lengthIn(0, 200),
        { propertyValue -> EmailBodyText(propertyValue) }
    )

val emailFromConfiguration: (PropertySource) -> PropertyConfiguration<EmailFrom> =
    makePropertyConfiguration(
        EMAIL_FROM,
        isEmail,
        { propertyValue -> EmailFrom(InternetAddress(propertyValue, true)) }
    )

val emailPasswordConfiguration: (PropertySource) -> PropertyConfiguration<EmailPassword> =
    makePropertyConfiguration(
        EMAIL_PASSWORD,

        notBlank compose noCrLfsTabs compose lengthIn(8, 100),
        { propertyValue -> EmailPassword(propertyValue) }
    )

val emailToConfiguration: (PropertySource) -> PropertyConfiguration<EmailTo> =
    makePropertyConfiguration(
        EMAIL_TO,
        isEmail,
        { propertyValue -> EmailTo(InternetAddress(propertyValue, true)) }
    )

val emailUserNameConfiguration: (PropertySource) -> PropertyConfiguration<EmailUserName> =
    makePropertyConfiguration(
        EMAIL_USER_NAME,
        notBlank compose noCrLfsTabs compose lengthIn(8, 50),
        { propertyValue -> EmailUserName(propertyValue) }
    )

val inputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<InputGatewayPattern> =
    makePropertyConfiguration(
        INPUT_GATEWAY_PATTERN,
        notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
        { propertyValue -> InputGatewayPattern(Pattern.compile(propertyValue)) }
    )

val MaximumNotifyDurationSeconds: (PropertySource) -> PropertyConfiguration<MaximumNotifyDurationSeconds> =
    makePropertyConfiguration(
        MAXIMUM_NOTIFY_DURATION_SECONDS,
        inClosedInterval(1, 60),
        { propertyValue -> MaximumNotifyDurationSeconds(propertyValue.toLong()) }
    )

val outputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<OutputGatewayPattern> =
    makePropertyConfiguration(
        OUTPUT_GATEWAY_PATTERN,
        notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
        { propertyValue -> OutputGatewayPattern(Pattern.compile(propertyValue)) }
    )

val streetNameConfiguration: (PropertySource) -> PropertyConfiguration<StreetName> =
    makePropertyConfiguration(
        STREET_NAME,
        notBlank compose noCrLfsTabs compose lengthIn(4, 50),
        { propertyValue -> StreetName(propertyValue) }
    )

val subjectTemplateConfiguration: (PropertySource) -> PropertyConfiguration<SubjectTemplate> =
    makePropertyConfiguration(
        SUBJECT_TEMPLATE,
        notBlank compose noCrLfsTabs compose lengthIn(10, 100),
        { propertyValue -> SubjectTemplate(propertyValue) }
    )

val worksheetsSearchDirectoryConfiguration: (PropertySource) -> PropertyConfiguration<WorkSheetsSearchDirectory> =
    makePropertyConfiguration(
        WORKSHEETS_SEARCH_DIRECTORY,
        fileExists compose isDirectory,
        { propertyValue -> WorkSheetsSearchDirectory(File(propertyValue)) }
    )

val streetNameSearchTermConfiguration: (PropertySource) -> PropertyConfiguration<StreetNameSearchTerm> =
    makePropertyConfiguration(
        STREET_NAME_SEARCH_TERM,
        notBlank compose noCrLfsTabs compose lengthIn(5, 50),
        { propertyValue -> StreetNameSearchTerm(propertyValue) }
    )

val postCodeSearchTermConfiguration: (PropertySource) -> PropertyConfiguration<PostCodeSearchTerm> =
    makePropertyConfiguration(
        POST_CODE_SEARCH_TERM,
        notBlank compose noCrLfsTabs compose lengthIn(5, 50),
        { propertyValue -> PostCodeSearchTerm(propertyValue) }
    )

val startUrlConfiguration: (PropertySource) -> PropertyConfiguration<StartUrl> =
    makePropertyConfiguration(
        START_URL,
        isUrl,
        { propertyValue -> StartUrl(URL(propertyValue)) }
    )

val waitDurationSecondsConfiguration: (PropertySource) -> PropertyConfiguration<WaitDurationSeconds> =
    makePropertyConfiguration(
        WAIT_DURATION_SECONDS,
        inClosedInterval(1, Long.MAX_VALUE),
        { propertyValue -> WaitDurationSeconds(propertyValue.toLong()) }
    )
