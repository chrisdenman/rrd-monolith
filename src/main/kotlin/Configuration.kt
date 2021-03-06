package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import kotlinx.coroutines.runBlocking
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
import uk.co.ceilingcat.rrd.monolith.ConfigurationException.CouldNotCreateConfigurationException
import java.util.regex.Pattern

data class InputGatewayPattern(val pattern: Pattern)
data class OutputGatewayPattern(val pattern: Pattern)

sealed class ConfigurationException(override val cause: Throwable?) : Throwable() {
    data class CouldNotCreateConfigurationException(override val cause: Throwable) : ConfigurationException(cause)
}

typealias CouldNotCreateConfiguration = CouldNotCreateConfigurationException

interface Configuration {
    val applicationFactoryFactoryClassName: ApplicationFactoryFactoryClassName
    val calendarName: CalendarName
    val calendarSummaryTemplate: CalendarSummaryTemplate
    val emailBodyText: EmailBodyText
    val emailFrom: EmailFrom
    val emailPassword: EmailPassword
    val emailTo: EmailTo
    val emailUserName: EmailUserName
    val inputGatewayPattern: InputGatewayPattern
    val maximumNotifyDurationSeconds: MaximumNotifyDurationSeconds
    val outputGatewayPattern: OutputGatewayPattern
    val streetName: StreetName
    val subjectTemplate: SubjectTemplate
    val worksheetsSearchDirectory: WorkSheetsSearchDirectory
    val streetNameSearchTerm: StreetNameSearchTerm
    val postCodeSearchTerm: PostCodeSearchTerm
    val startUrl: StartUrl
    val waitDurationSeconds: WaitDurationSeconds
    val driverProperty: DriverProperty
    val driverLocation: DriverLocation
    val driverOptions: DriverOptions
}

private data class ConfigurationData(
    override val applicationFactoryFactoryClassName: ApplicationFactoryFactoryClassName,
    override val calendarName: CalendarName,
    override val calendarSummaryTemplate: CalendarSummaryTemplate,
    override val driverLocation: DriverLocation,
    override val driverOptions: DriverOptions,
    override val driverProperty: DriverProperty,
    override val emailBodyText: EmailBodyText,
    override val emailFrom: EmailFrom,
    override val emailPassword: EmailPassword,
    override val emailTo: EmailTo,
    override val emailUserName: EmailUserName,
    override val inputGatewayPattern: InputGatewayPattern,
    override val maximumNotifyDurationSeconds: MaximumNotifyDurationSeconds,
    override val outputGatewayPattern: OutputGatewayPattern,
    override val streetName: StreetName,
    override val subjectTemplate: SubjectTemplate,
    override val worksheetsSearchDirectory: WorkSheetsSearchDirectory,
    override val streetNameSearchTerm: StreetNameSearchTerm,
    override val postCodeSearchTerm: PostCodeSearchTerm,
    override val startUrl: StartUrl,
    override val waitDurationSeconds: WaitDurationSeconds,
) : Configuration

typealias PropertyCreator<T> = (PropertyValue) -> T
typealias PropertyFetcher = Function0<ErrorOrPropertyValue>

// a type that encapsulates a property reference and the data needed to validate and construct it
class PropertyConfiguration<T>(
    val name: PropertyName,
    val fetcher: PropertyFetcher,
    val validator: PropertyValidator,
    val constructor: PropertyCreator<T>,
)

val propertySourceFetcher: (PropertySource, PropertyName) -> PropertyFetcher =
    { propertySource, propertyName ->
        {
            propertySource.getEither(propertyName)
        }
    }

fun <T> validateAndConstruct(propertyConfiguration: PropertyConfiguration<T>) =
    with(propertyConfiguration) {
        try {
            val propertyValue = fetcher()
            val validatedProperty = validator(propertyValue)
            val constructedProperty = validatedProperty.map { constructor(it) }
            constructedProperty.mapLeft {
                CouldNotConstructProperty(
                    "Could not validate and construct the '${this.name}' property.",
                    it
                )
            }
        } catch (t: Throwable) {
            CouldNotConstructProperty(
                "An exception was caught whilst validating and constructing the '${this.name}' property."
            ).left()
        }
    }

fun createConfiguration(
    applicationFactoryFactoryClassName: PropertyConfiguration<ApplicationFactoryFactoryClassName>,
    calendarName: PropertyConfiguration<CalendarName>,
    calendarSummaryTemplate: PropertyConfiguration<CalendarSummaryTemplate>,
    driverLocation: PropertyConfiguration<DriverLocation>,
    driverOptions: PropertyConfiguration<DriverOptions>,
    driverProperty: PropertyConfiguration<DriverProperty>,
    emailBodyText: PropertyConfiguration<EmailBodyText>,
    emailFrom: PropertyConfiguration<EmailFrom>,
    emailPassword: PropertyConfiguration<EmailPassword>,
    emailTo: PropertyConfiguration<EmailTo>,
    emailUserName: PropertyConfiguration<EmailUserName>,
    inputGatewayPattern: PropertyConfiguration<InputGatewayPattern>,
    streetName: PropertyConfiguration<StreetName>,
    maximumNotifyDurationSeconds: PropertyConfiguration<MaximumNotifyDurationSeconds>,
    outputGatewayPattern: PropertyConfiguration<OutputGatewayPattern>,
    subjectTemplate: PropertyConfiguration<SubjectTemplate>,
    worksheetsSearchDirectory: PropertyConfiguration<WorkSheetsSearchDirectory>,
    streetNameSearchTerm: PropertyConfiguration<StreetNameSearchTerm>,
    postCodeSearchTerm: PropertyConfiguration<PostCodeSearchTerm>,
    startUrl: PropertyConfiguration<StartUrl>,
    waitDurationSeconds: PropertyConfiguration<WaitDurationSeconds>
): Either<CouldNotCreateConfiguration, Configuration> =
    runBlocking {
        either.eager<PropertyError, Configuration> {
            val v0 = !validateAndConstruct(applicationFactoryFactoryClassName)
            val v1 = !validateAndConstruct(calendarName)
            val v2 = !validateAndConstruct(calendarSummaryTemplate)
            val v3 = !validateAndConstruct(emailBodyText)
            val v4 = !validateAndConstruct(emailFrom)
            val v5 = !validateAndConstruct(emailPassword)
            val v6 = !validateAndConstruct(emailTo)
            val v7 = !validateAndConstruct(emailUserName)
            val v8 = !validateAndConstruct(inputGatewayPattern)
            val v9 = !validateAndConstruct(maximumNotifyDurationSeconds)
            val v10 = !validateAndConstruct(outputGatewayPattern)
            val v11 = !validateAndConstruct(streetName)
            val v12 = !validateAndConstruct(subjectTemplate)
            val v13 = !validateAndConstruct(worksheetsSearchDirectory)
            val v14 = !validateAndConstruct(streetNameSearchTerm)
            val v15 = !validateAndConstruct(postCodeSearchTerm)
            val v16 = !validateAndConstruct(startUrl)
            val v17 = !validateAndConstruct(driverProperty)
            val v18 = !validateAndConstruct(driverLocation)
            val v19 = !validateAndConstruct(driverOptions)
            val v20 = !validateAndConstruct(waitDurationSeconds)

            ConfigurationData(
                v0, v1, v2, v18, v19, v17, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v20
            )
        }
    }.mapLeft { CouldNotCreateConfiguration(it) }
