package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.Either
import org.junit.jupiter.api.Assertions

infix fun Any.assertEquals(actual: Any) = Assertions.assertEquals(this, actual)

infix fun Any.assertLeft(actual: Any) = Either.left(this) assertEquals actual

infix fun Any.assertRight(actual: Any) = Either.right(this) assertEquals actual
