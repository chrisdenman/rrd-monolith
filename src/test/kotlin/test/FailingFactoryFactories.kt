package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import uk.co.ceilingcat.rrd.entities.ServiceDetails
import uk.co.ceilingcat.rrd.monolith.Configuration
import uk.co.ceilingcat.rrd.monolith.FactoryConstructionError
import uk.co.ceilingcat.rrd.monolith.SimpleApplicationFactoryFactory
import uk.co.ceilingcat.rrd.usecases.NextUpcomingInputGateway
import uk.co.ceilingcat.rrd.usecases.UpcomingOutputGateway
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
open class AllInputGatewaysFail : SimpleApplicationFactoryFactory() {
    override fun createInputGateways(configuration: Configuration): Either<FactoryConstructionError, List<NextUpcomingInputGateway>> {
        return right(
            listOf(
                object : NextUpcomingInputGateway {
                    override fun nextUpcoming(): Either<Throwable, ServiceDetails?> = left(Throwable())
                }
            )
        )
    }
}

@ExperimentalPathApi
open class AllOutputGatewaysFail : SimpleApplicationFactoryFactory() {
    override fun createOutputGateways(configuration: Configuration): Either<FactoryConstructionError, List<UpcomingOutputGateway>> {
        return right(
            listOf(
                object : UpcomingOutputGateway {
                    override fun notify(serviceDetails: ServiceDetails): Either<Throwable, Unit> = left(Throwable())
                }
            )
        )
    }
}

@ExperimentalPathApi
class NoInputGateways : AllOutputGatewaysFail() {
    override fun createInputGateways(configuration: Configuration): Either<FactoryConstructionError, List<NextUpcomingInputGateway>> =
        right(emptyList())
}

@ExperimentalPathApi
class NoOutputGateways : AllInputGatewaysFail() {
    override fun createOutputGateways(configuration: Configuration): Either<FactoryConstructionError, List<UpcomingOutputGateway>> =
        right(emptyList())
}
