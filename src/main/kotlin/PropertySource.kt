package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either.Companion.conditionally
import java.util.Properties

typealias PropertyName = String
typealias PropertyValue = String

interface PropertySource {
    fun getEither(propertyName: PropertyName): ErrorOrPropertyValue
}

val createPropertySource: (Properties) -> PropertySource = {
    object : PropertySource {
        override fun getEither(propertyName: PropertyName): ErrorOrPropertyValue =
            with(it) {
                conditionally(
                    containsKey(propertyName),
                    { NoSuchProperty("There is no property with name '$propertyName'.") },
                    { getProperty(propertyName) }
                )
            }
    }
}
