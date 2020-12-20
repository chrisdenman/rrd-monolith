package uk.co.ceilingcat.rrd.monolith.test

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.core.flatMap
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.APP_TEST_PROPERTIES_PATH
import uk.co.ceilingcat.rrd.monolith.test.TestData.Companion.PROPERTIES_PATH_PROPERTY_NAME
import java.io.File
import java.util.Properties
import java.util.UUID

fun Properties.set(keyToValues: Map<String, String>) = PropertiesHelper.setProperties(keyToValues, this)

class PropertiesHelper {

    companion object {

        data class BootstrapProperties(
            val bootstrapProperties: Properties,
            val applicationPropertiesAndLocation: PropertiesAndLocation
        )

        data class PropertiesAndLocation(
            val properties: Properties,
            val location: File
        )

        fun removeProperty(keyName: String, properties: Properties) = removeProperties(listOf(keyName), properties)

        private fun removeProperties(keyNames: List<String>, properties: Properties) = properties.apply {
            keyNames.forEach { remove(it) }
        }

        fun setProperty(propertyName: String, propertyValue: String, properties: Properties) =
            setProperties(mapOf(propertyName to propertyValue), properties)

        fun setProperties(keyToValues: Map<String, String>, properties: Properties) = properties.apply {
            keyToValues.forEach { (key, value) ->
                setProperty(key, value)
            }
        }

        fun loadProperties(propertiesFile: File): Either<Throwable, Properties> = try {
            right(
                propertiesFile
                    .inputStream()
                    .use { fis ->
                        Properties().apply {
                            load(fis)
                        }
                    }
            )
        } catch (t: Throwable) {
            left(t)
        }

        fun saveProperties(propertiesAndLocation: PropertiesAndLocation): Either<Throwable, PropertiesAndLocation> = try {
            right(
                with(propertiesAndLocation) {
                    location.outputStream().use { fos ->
                        properties.store(fos, null)
                        this
                    }
                }
            )
        } catch (t: Throwable) {
            left(t)
        }

        fun createTempFile(prefix: String = UUID.randomUUID().toString()): Either<Throwable, File> =
            try {
                right(File.createTempFile(prefix, ".properties").also { it.deleteOnExit() })
            } catch (t: Throwable) {
                left(t)
            }

        fun mutateProperties(
            propertiesMutator: (Properties) -> Unit,
            propertyPath: String = APP_TEST_PROPERTIES_PATH
        ): Either<Throwable, BootstrapProperties> =
            loadProperties(File(propertyPath))
                .flatMap { properties ->
                    propertiesMutator(properties)
                    createTempFile()
                        .bimap({ it }) { PropertiesAndLocation(properties, it) }
                }.flatMap(::saveProperties)
                .flatMap {
                    right(
                        BootstrapProperties(
                            setProperty(
                                PROPERTIES_PATH_PROPERTY_NAME,
                                it.location.canonicalPath,
                                Properties()
                            ),
                            it
                        )
                    )
                }
    }
}
