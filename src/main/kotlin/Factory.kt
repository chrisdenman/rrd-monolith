package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.conditionally
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.createCalendarOutputGateway
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.createEMailOutputGateway
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.Driver
import uk.co.ceilingcat.rrd.gateways.htmlinputgateway.createHtmlInputGateway
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.createXlsxInputGateway
import uk.co.ceilingcat.rrd.monolith.FactoryException.CouldNotCreateFactoryClassException
import uk.co.ceilingcat.rrd.monolith.FactoryException.CouldNotCreateUseCaseException
import uk.co.ceilingcat.rrd.monolith.FactoryException.NoInputGatewaysException
import uk.co.ceilingcat.rrd.monolith.FactoryException.NoOutputGatewaysException
import uk.co.ceilingcat.rrd.usecases.CurrentDate
import uk.co.ceilingcat.rrd.usecases.NextUpcomingInputGateway
import uk.co.ceilingcat.rrd.usecases.NotifyOfNextServiceDate
import uk.co.ceilingcat.rrd.usecases.UpcomingOutputGateway
import uk.co.ceilingcat.rrd.usecases.createCurrentDate
import uk.co.ceilingcat.rrd.usecases.createNotifyOfNextServiceDate
import kotlin.io.path.ExperimentalPathApi
import kotlin.reflect.full.createInstance

data class ApplicationFactoryFactoryClassName(val text: String)

interface ApplicationFactoryFactory {
    fun create(configuration: Configuration): Either<FactoryError, Factory>
}

internal fun createApplicationFactory(configuration: Configuration): Either<FactoryError, Factory> =
    configuration.applicationFactoryFactoryClassName.text.let { applicationFactoryFactoryClassName ->
        try {
            (
                Class
                    .forName(applicationFactoryFactoryClassName)
                    .kotlin
                    .createInstance() as ApplicationFactoryFactory
                ).create(configuration)
        } catch (t: Throwable) {
            CouldNotCreateFactoryClass(
                "Could not load the application factory '$applicationFactoryFactoryClassName'."
            ).left()
        }
    }

sealed class FactoryException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable() {
    data class CouldNotCreateFactoryClassException(override val message: String) : FactoryException()
    data class NoInputGatewaysException(override val message: String) : FactoryException()
    data class NoOutputGatewaysException(override val message: String) : FactoryException()
    data class CouldNotCreateUseCaseException(override val cause: Throwable) : FactoryException()
}

typealias FactoryError = FactoryException
typealias CouldNotCreateFactoryClass = CouldNotCreateFactoryClassException
typealias NoInputGateways = NoInputGatewaysException
typealias NoOutputGateways = NoOutputGatewaysException
typealias CouldNotCreateUseCase = CouldNotCreateUseCaseException

interface Factory {
    val currentDate: CurrentDate
    val nextUpcomingInputGateways: List<NextUpcomingInputGateway>
    val upcomingOutputGateways: List<UpcomingOutputGateway>
    val notifyOfNextServiceDate: NotifyOfNextServiceDate
}

@ExperimentalPathApi
@Suppress("unused")
open class SimpleApplicationFactoryFactory : ApplicationFactoryFactory {

    override fun create(configuration: Configuration): Either<FactoryError, Factory> =
        createInputGateways(configuration).flatMap { inputGateways ->
            createOutputGateways(configuration).flatMap { outputGateways ->
                createCurrentDate().let { currentDate ->
                    createNotifyOfNextServiceDate(currentDate, inputGateways, outputGateways)
                        .flatMap { notifyOfNextServiceDate ->
                            object : Factory {
                                override val currentDate: CurrentDate = currentDate
                                override val nextUpcomingInputGateways = inputGateways
                                override val upcomingOutputGateways = outputGateways
                                override val notifyOfNextServiceDate = notifyOfNextServiceDate
                            }.right()
                        }.mapLeft { error -> CouldNotCreateUseCase(error) }
                }
            }
        }

    open fun createInputGateways(configuration: Configuration): Either<NoInputGateways, List<NextUpcomingInputGateway>> =
        configuration.run {
            mapOf(
                "HtmlInputGateway" to {
                    createHtmlInputGateway(
                        startUrl,
                        streetNameSearchTerm,
                        postCodeSearchTerm,
                        Driver(driverProperty, driverLocation, driverOptions),
                        waitDurationSeconds
                    )
                },
                "XlsxInputGateway" to {
                    createXlsxInputGateway(
                        createCurrentDate(),
                        streetName,
                        worksheetsSearchDirectory
                    )
                }
            )
                .filter { inputGatewayPattern.pattern.asMatchPredicate().test(it.key) }
                .values
                .toList()
                .let { inputGateways ->
                    conditionally(
                        inputGateways.isNotEmpty(),
                        {
                            NoInputGateways(
                                "No input gateways were available using the pattern " +
                                    "'${inputGatewayPattern.pattern.toRegex().pattern}'."
                            )
                        }
                    ) { inputGateways.map { it() } }
                }
        }

    open fun createOutputGateways(configuration: Configuration): Either<NoOutputGateways, List<UpcomingOutputGateway>> =
        configuration.run {
            mapOf(
                "EMailOutputGateway" to {
                    createEMailOutputGateway(
                        emailUserName,
                        emailPassword,
                        emailFrom,
                        emailTo,
                        emailBodyText,
                        subjectTemplate
                    )
                },
                "CalendarOutputGateway" to {
                    createCalendarOutputGateway(
                        calendarName,
                        calendarSummaryTemplate,
                        maximumNotifyDurationSeconds
                    )
                }
            ).filter { outputGatewayPattern.pattern.asMatchPredicate().test(it.key) }
                .values
                .toList()
                .let { outputGateways ->
                    conditionally(
                        outputGateways.isNotEmpty(),
                        {
                            NoOutputGateways(
                                "No output gateways were available using the pattern " +
                                    "'${outputGatewayPattern.pattern.toRegex().pattern}'."
                            )
                        }
                    ) { outputGateways.map { it() } }
                }
        }
}
