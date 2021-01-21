package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions

infix fun Any.assertEquals(actual: Any) = Assertions.assertEquals(this, actual)
infix fun Any.assertLeft(actual: Any) = this.left() assertEquals actual
infix fun Any.assertRight(actual: Any) = this.right() assertEquals actual

infix fun Throwable.assertEquals(actual: Throwable) =
    message == actual.message &&
        cause?.javaClass == actual.javaClass &&
        cause?.message == actual.message
