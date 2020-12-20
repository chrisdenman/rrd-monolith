package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.flatMap
import java.io.File
import java.util.regex.Pattern
import javax.mail.internet.InternetAddress

sealed class PropertyExceptions : Throwable() {
    object PropertyException : PropertyExceptions()
}
typealias PropertyError = PropertyExceptions.PropertyException

typealias ErrorOrPropertyValue = Either<PropertyError, PropertyValue>
typealias PropertyValuePredicate = (propertyValue: PropertyValue) -> Boolean

// Closed => Magma(Transformation), Semigroup (is it associative), there's no identity so not a Monoid
typealias PropertyValidator = (ErrorOrPropertyValue) -> ErrorOrPropertyValue
typealias PredicatedPropertyValidator = (PropertyValuePredicate) -> PropertyValidator

val predicated: PredicatedPropertyValidator = { predicate ->
    {
        it.flatMap { propertyValue ->
            Either.conditionally(predicate(propertyValue), { PropertyError }, { propertyValue })
        }
    }
}

val catching: (PropertyValidator) -> PropertyValidator = { propertyValidator ->
    { it: ErrorOrPropertyValue ->
        try {
            propertyValidator(it)
        } catch (t: Throwable) {
            Either.left(PropertyError)
        }
    }
}

val isFqcn: PropertyValidator =
    catching(predicated { Pattern.compile("([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*").asMatchPredicate().test(it) })
val notBlank: PropertyValidator = predicated { it == "" || !it.isBlank() }
val notEmpty: PropertyValidator = predicated { it.isNotEmpty() }
val fileExists: PropertyValidator = predicated { File(it).exists() }
val isDirectory: PropertyValidator = predicated { File(it).isDirectory }
val inClosedInterval: (min: Long, max: Long) -> PropertyValidator =
    { min, max -> catching(predicated { it.toLong() in min..max }) }
val isRegexPattern: PropertyValidator = catching(predicated { Pattern.compile(it); true })
val isEmail: PropertyValidator = catching(predicated { InternetAddress(it, true); true })
val noCrLfsTabs: PropertyValidator = predicated { it.find { it in listOf('\r', '\n', '\t') } == null }
val lengthIn: (min: Long, max: Long) -> PropertyValidator =
    { min, max -> catching(predicated { it.length in min..max }) }
