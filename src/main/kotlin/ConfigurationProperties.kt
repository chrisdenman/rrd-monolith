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
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.StreetName
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.WorkSheetsSearchDirectory
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.APPLICATION_FACTORY_FACTORY_CLASS_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.CALENDAR_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.CALENDAR_SUMMARY_TEMPLATE
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_BODY_TEXT
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_FROM
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_PASSWORD
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_TO
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.EMAIL_USER_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.INPUT_GATEWAY_PATTERN
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.MAXIMUM_NOTIFY_DURATION_SECONDS
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.OUTPUT_GATEWAY_PATTERN
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.STREET_NAME
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.SUBJECT_TEMPLATE
import uk.co.ceilingcat.rrd.monolith.ConfigurationProperties.WORKSHEETS_SEARCH_DIRECTORY
import java.io.File
import java.util.regex.Pattern
import javax.mail.internet.InternetAddress

enum class ConfigurationProperties(val propertyName: PropertyName) {
    APPLICATION_FACTORY_FACTORY_CLASS_NAME("APPLICATION_FACTORY_FACTORY_CLASS_NAME"),
    CALENDAR_NAME("CALENDAR_NAME"),
    CALENDAR_SUMMARY_TEMPLATE("CALENDAR_SUMMARY_TEMPLATE"),
    EMAIL_BODY_TEXT("EMAIL_BODY_TEXT"),
    EMAIL_FROM("EMAIL_FROM"),
    EMAIL_PASSWORD("EMAIL_PASSWORD"),
    EMAIL_TO("EMAIL_TO"),
    EMAIL_USER_NAME("EMAIL_USER_NAME"),
    INPUT_GATEWAY_PATTERN("INPUT_GATEWAY_PATTERN"),
    MAXIMUM_NOTIFY_DURATION_SECONDS("MAXIMUM_NOTIFY_DURATION_SECONDS"),
    OUTPUT_GATEWAY_PATTERN("OUTPUT_GATEWAY_PATTERN"),
    STREET_NAME("STREET_NAME"),
    SUBJECT_TEMPLATE("SUBJECT_TEMPLATE"),
    WORKSHEETS_SEARCH_DIRECTORY("WORKSHEETS_SEARCH_DIRECTORY")
}

val applicationFactoryFactoryClassNameConfiguration: (PropertySource) -> PropertyConfiguration<ApplicationFactoryFactoryClassName> =
    {
        PropertyConfiguration(
            propertySourceFetcher(it, APPLICATION_FACTORY_FACTORY_CLASS_NAME.propertyName),
            isFqcn compose lengthIn(1, 300),
            { propertyValue -> ApplicationFactoryFactoryClassName(propertyValue) }
        )
    }

val calendarNameConfiguration: (PropertySource) -> PropertyConfiguration<CalendarName> = {
    PropertyConfiguration(
        propertySourceFetcher(it, CALENDAR_NAME.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(1, 100),
        { propertyValue -> CalendarName(propertyValue) }
    )
}

val calendarSummaryTemplateConfiguration: (PropertySource) -> PropertyConfiguration<CalendarSummaryTemplate> = {
    PropertyConfiguration(
        propertySourceFetcher(it, CALENDAR_SUMMARY_TEMPLATE.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(1, 100),
        { propertyValue -> CalendarSummaryTemplate(propertyValue) }
    )
}

val emailBodyTextConfiguration: (PropertySource) -> PropertyConfiguration<EmailBodyText> = {
    PropertyConfiguration(
        propertySourceFetcher(it, EMAIL_BODY_TEXT.propertyName),
        notBlank compose lengthIn(0, 200),
        { propertyValue -> EmailBodyText(propertyValue) }
    )
}

val emailFromConfiguration: (PropertySource) -> PropertyConfiguration<EmailFrom> = {
    PropertyConfiguration(
        propertySourceFetcher(it, EMAIL_FROM.propertyName),
        isEmail,
        { propertyValue -> EmailFrom(InternetAddress(propertyValue, true)) }
    )
}

val emailPasswordConfiguration: (PropertySource) -> PropertyConfiguration<EmailPassword> = {
    PropertyConfiguration(
        propertySourceFetcher(it, EMAIL_PASSWORD.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(8, 100),
        { propertyValue -> EmailPassword(propertyValue) }
    )
}

val emailToConfiguration: (PropertySource) -> PropertyConfiguration<EmailTo> = {
    PropertyConfiguration(
        propertySourceFetcher(it, EMAIL_TO.propertyName),
        isEmail,
        { propertyValue -> EmailTo(InternetAddress(propertyValue, true)) }
    )
}

val emailUserNameConfiguration: (PropertySource) -> PropertyConfiguration<EmailUserName> = {
    PropertyConfiguration(
        propertySourceFetcher(it, EMAIL_USER_NAME.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(8, 50),
        { propertyValue -> EmailUserName(propertyValue) }
    )
}

val inputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<InputGatewayPattern> = {
    PropertyConfiguration(
        propertySourceFetcher(it, INPUT_GATEWAY_PATTERN.propertyName),
        notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
        { propertyValue -> InputGatewayPattern(Pattern.compile(propertyValue)) }
    )
}

val MaximumNotifyDurationSeconds: (PropertySource) -> PropertyConfiguration<MaximumNotifyDurationSeconds> = {
    PropertyConfiguration(
        propertySourceFetcher(it, MAXIMUM_NOTIFY_DURATION_SECONDS.propertyName),
        inClosedInterval(1, 60),
        { propertyValue -> MaximumNotifyDurationSeconds(propertyValue.toLong()) }
    )
}

val outputGatewayPatternConfiguration: (PropertySource) -> PropertyConfiguration<OutputGatewayPattern> = {
    PropertyConfiguration(
        propertySourceFetcher(it, OUTPUT_GATEWAY_PATTERN.propertyName),
        notBlank compose noCrLfsTabs compose isRegexPattern compose lengthIn(1, 1024),
        { propertyValue -> OutputGatewayPattern(Pattern.compile(propertyValue)) }
    )
}

val streetNameConfiguration: (PropertySource) -> PropertyConfiguration<StreetName> = {
    PropertyConfiguration(
        propertySourceFetcher(it, STREET_NAME.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(4, 50),
        { propertyValue -> StreetName(propertyValue) }
    )
}

val subjectTemplateConfiguration: (PropertySource) -> PropertyConfiguration<SubjectTemplate> = {
    PropertyConfiguration(
        propertySourceFetcher(it, SUBJECT_TEMPLATE.propertyName),
        notBlank compose noCrLfsTabs compose lengthIn(10, 100),
        { propertyValue -> SubjectTemplate(propertyValue) }
    )
}

val worksheetsSearchDirectoryConfiguration: (PropertySource) -> PropertyConfiguration<WorkSheetsSearchDirectory> = {
    PropertyConfiguration(
        propertySourceFetcher(it, WORKSHEETS_SEARCH_DIRECTORY.propertyName),
        fileExists compose isDirectory,
        { propertyValue -> WorkSheetsSearchDirectory(File(propertyValue)) }
    )
}
