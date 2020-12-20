package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import java.util.Properties

typealias PropertyName = String
typealias PropertyValue = String

interface PropertySource {
    fun getEither(propertyName: PropertyName): ErrorOrPropertyValue
}

val createPropertySource: (Properties) -> PropertySource = {
    object : PropertySource {
        private val properties = it

        override fun getEither(propertyName: PropertyName): ErrorOrPropertyValue =
            with(properties) {
                when (containsKey(propertyName)) {
                    true -> right(getProperty(propertyName))
                    else -> left(PropertyError)
                }
            }
    }
}
