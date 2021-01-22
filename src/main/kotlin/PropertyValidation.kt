package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.conditionally
import arrow.core.flatMap
import java.io.File
import java.net.URL
import java.util.regex.Pattern
import javax.mail.internet.InternetAddress

sealed class PropertyException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable() {
    data class NoSuchPropertyException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : PropertyException()
    data class PropertyValidationException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : PropertyException()
    data class CouldNotCreateProertyException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : PropertyException()
}
typealias PropertyError = PropertyException
typealias NoSuchProperty = PropertyException.NoSuchPropertyException
typealias PropertyFailedValidation = PropertyException.PropertyValidationException
typealias CouldNotCreateProperty = PropertyException.CouldNotCreateProertyException

typealias ErrorOrPropertyValue = Either<PropertyError, PropertyValue>
typealias PropertyValuePredicate = (propertyValue: PropertyValue) -> Boolean

// Closed => Magma(Transformation), Semigroup (is it associative), there's no identity so not a Monoid
typealias PropertyValidator = (ErrorOrPropertyValue) -> ErrorOrPropertyValue
typealias PredicatedPropertyValidator = (PropertyValuePredicate) -> PropertyValidator

val predicated: PredicatedPropertyValidator = { predicate ->
    {
        it.flatMap { propertyValue ->
            conditionally(
                predicate(propertyValue),
                { PropertyFailedValidation("The property value '$propertyValue' failed validation.") },
                { propertyValue }
            )
        }
    }
}

val fileExists: PropertyValidator = predicated { File(it).exists() }
val inClosedInterval: (min: Long, max: Long) -> PropertyValidator =
    { min, max -> predicated { it.toLong() in min..max } }
val isRegexPattern: PropertyValidator = predicated { Pattern.compile(it); true }
val isDirectory: PropertyValidator = predicated { File(it).isDirectory }
val isEmail: PropertyValidator = predicated { InternetAddress(it, true); true }
val isFqcn: PropertyValidator = predicated {
    Pattern
        .compile("([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*")
        .asMatchPredicate()
        .test(it)
}
val isUrl: PropertyValidator = predicated { URL(it); true }
val lengthIn: (min: Long, max: Long) -> PropertyValidator =
    { min, max -> predicated { it.length in min..max } }
val notBlank: PropertyValidator = predicated { it == "" || it.isNotBlank() }
val noCrLfsTabs: PropertyValidator = predicated {
    it.find { theChar ->
        theChar in listOf('\r', '\n', '\t')
    } == null
}
