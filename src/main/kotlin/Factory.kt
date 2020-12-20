package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.core.computations.either
import kotlinx.coroutines.runBlocking
import uk.co.ceilingcat.rrd.gateways.calendaroutputgateway.createCalendarOutputGateway
import uk.co.ceilingcat.rrd.gateways.emailoutputgateway.createEMailOutputGateway
import uk.co.ceilingcat.rrd.gateways.xlsxinputgateway.createXlsxInputGateway
import uk.co.ceilingcat.rrd.monolith.FactoryException.FactoryConstructionException
import uk.co.ceilingcat.rrd.usecases.CurrentDate
import uk.co.ceilingcat.rrd.usecases.InputGateway
import uk.co.ceilingcat.rrd.usecases.NextUpcomingInputGateway
import uk.co.ceilingcat.rrd.usecases.NotifyOfNextServiceDate
import uk.co.ceilingcat.rrd.usecases.UpcomingOutputGateway
import uk.co.ceilingcat.rrd.usecases.createCurrentDate
import uk.co.ceilingcat.rrd.usecases.createNotifyOfNextServiceDate
import kotlin.io.path.ExperimentalPathApi
import kotlin.reflect.full.createInstance

data class ApplicationFactoryFactoryClassName(val text: String)

typealias InputGatewayFactoryConstructor = (Configuration) -> InputGateway

interface InputGatewayFactory {
    val constructor: InputGatewayFactoryConstructor
}

interface ApplicationFactoryFactory {
    fun create(configuration: Configuration): Either<FactoryConstructionError, Factory>
}

internal fun createApplicationFactory(configuration: Configuration): Either<FactoryConstructionError, Factory> =
    try {
        (
            Class.forName(configuration.applicationFactoryFactoryClassName.text)
                .kotlin
                .createInstance() as ApplicationFactoryFactory
            ).create(configuration)
    } catch (t: Throwable) {
        left(FactoryConstructionError)
    }

sealed class FactoryException : Throwable() {
    object FactoryConstructionException : FactoryException()
}

typealias FactoryConstructionError = FactoryConstructionException

interface Factory {
    val currentDate: CurrentDate
    val nextUpcomingInputGateways: List<NextUpcomingInputGateway>
    val upcomingOutputGateways: List<UpcomingOutputGateway>
    val notifyOfNextServiceDate: NotifyOfNextServiceDate
}

@ExperimentalPathApi
@Suppress("unused")
open class SimpleApplicationFactoryFactory : ApplicationFactoryFactory {

    override fun create(configuration: Configuration): Either<FactoryConstructionError, Factory> =
        runBlocking {
            either.eager<FactoryConstructionError, Factory> {
                val currentDate = createCurrentDate()
                val inputGateways = !createInputGateways(configuration)
                val outputGateways = !createOutputGateways(configuration)
                val notifyOfNextServiceDate = !createNotifyOfNextServiceDate(
                    currentDate,
                    inputGateways,
                    outputGateways
                ).mapLeft { FactoryConstructionError }
                object : Factory {
                    override val currentDate: CurrentDate = currentDate
                    override val nextUpcomingInputGateways = inputGateways
                    override val upcomingOutputGateways = outputGateways
                    override val notifyOfNextServiceDate = notifyOfNextServiceDate
                }
            }
        }

    open fun createInputGateways(configuration: Configuration): Either<FactoryConstructionError, List<NextUpcomingInputGateway>> =
        configuration.run {
            mapOf(
                "XlsxInputGateway" to {
                    createXlsxInputGateway(
                        createCurrentDate(),
                        streetName,
                        worksheetsSearchDirectory
                    )
                }
            ).filter {
                inputGatewayPattern.pattern.asMatchPredicate().test(it.key)
            }.values.toList().let {
                if (it.isEmpty()) left(FactoryConstructionError) else right(it.map { it() })
            }
        }

    open fun createOutputGateways(configuration: Configuration): Either<FactoryConstructionError, List<UpcomingOutputGateway>> =
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
            ).filter {
                outputGatewayPattern.pattern.asMatchPredicate().test(it.key)
            }.values.toList().let {
                if (it.isEmpty()) left(FactoryConstructionError) else right(it.map { it() })
            }
        }.mapLeft { FactoryConstructionError }
}
