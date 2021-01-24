package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.co.ceilingcat.rrd.entities.ServiceDetails
import uk.co.ceilingcat.rrd.monolith.Configuration
import uk.co.ceilingcat.rrd.monolith.NoInputGateways
import uk.co.ceilingcat.rrd.monolith.NoOutputGateways
import uk.co.ceilingcat.rrd.monolith.SimpleApplicationFactoryFactory
import uk.co.ceilingcat.rrd.usecases.NextUpcomingInputGateway
import uk.co.ceilingcat.rrd.usecases.UpcomingOutputGateway
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
open class AllInputGatewaysFail : SimpleApplicationFactoryFactory() {
    override fun createInputGateways(configuration: Configuration): Either<NoInputGateways, List<NextUpcomingInputGateway>> =
        listOf(
            object : NextUpcomingInputGateway {
                override fun nextUpcoming(): Either<Throwable, ServiceDetails?> = Throwable().left()
            }
        ).right()
}

@ExperimentalPathApi
open class AllOutputGatewaysFail : SimpleApplicationFactoryFactory() {
    override fun createOutputGateways(configuration: Configuration): Either<NoOutputGateways, List<UpcomingOutputGateway>> =
        listOf(
            object : UpcomingOutputGateway {
                override fun notify(serviceDetails: ServiceDetails): Either<Throwable, Unit> = Throwable().left()
            }
        ).right()
}

@ExperimentalPathApi
class NoInputGateways : AllOutputGatewaysFail() {
    override fun createInputGateways(configuration: Configuration): Either<NoInputGateways, List<NextUpcomingInputGateway>> =
        emptyList<NextUpcomingInputGateway>().right()
}

@ExperimentalPathApi
class NoOutputGateways : AllInputGatewaysFail() {
    override fun createOutputGateways(configuration: Configuration): Either<NoOutputGateways, List<UpcomingOutputGateway>> =
        emptyList<UpcomingOutputGateway>().right()
}
