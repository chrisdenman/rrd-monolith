package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.core.flatMap
import java.io.File
import java.io.IOException
import java.util.Properties

sealed class RefuseRecyclingDatesException : Throwable() {
    object NoPropertyPathPropertyException : RefuseRecyclingDatesException()
    object NoPropertiesFileException : RefuseRecyclingDatesException()
    object PropertiesFileIsDirectoryException : RefuseRecyclingDatesException()
    object MalformedPropertiesFileException : RefuseRecyclingDatesException()
    object PropertiesFileLoadException : RefuseRecyclingDatesException()
    object ConfigurationException : RefuseRecyclingDatesException()
    object FactoryException : RefuseRecyclingDatesException()
    object ExecutionException : RefuseRecyclingDatesException()
}

typealias MainRefuseRecyclingDatesError = RefuseRecyclingDatesException
typealias MainNoPropertyPathPropertyError = RefuseRecyclingDatesException.NoPropertyPathPropertyException
typealias MainNoPropertiesFileError = RefuseRecyclingDatesException.NoPropertiesFileException
typealias MainPropertiesFileIsDirectoryError = RefuseRecyclingDatesException.PropertiesFileIsDirectoryException
typealias MainMalformedPropertiesFileError = RefuseRecyclingDatesException.MalformedPropertiesFileException
typealias MainPropertiesFileLoadError = RefuseRecyclingDatesException.PropertiesFileLoadException
typealias MainConfigurationError = RefuseRecyclingDatesException.ConfigurationException
typealias MainFactoryError = RefuseRecyclingDatesException.FactoryException
typealias MainExecutionError = RefuseRecyclingDatesException.ExecutionException

class Main(properties: Properties = System.getProperties()) {

    private val errorOrProperties: Either<MainRefuseRecyclingDatesError, Properties> = propertyPathProperty(properties)
        .flatMap { applicationPropertiesFile(it) }
        .flatMap { applicationPropertiesFileIsNotDirectory(it) }
        .flatMap { loadApplicationProperties(it) }

    val execute: Either<MainRefuseRecyclingDatesError, Unit> =
        errorOrProperties.flatMap {
            with(createPropertySource(it)) {
                createConfiguration(
                    applicationFactoryFactoryClassNameConfiguration(this),
                    calendarNameConfiguration(this),
                    calendarSummaryTemplateConfiguration(this),
                    driverLocationConfiguration(this),
                    driverOptionsConfiguration(this),
                    driverPropertyConfiguration(this),
                    emailBodyTextConfiguration(this),
                    emailFromConfiguration(this),
                    emailPasswordConfiguration(this),
                    emailToConfiguration(this),
                    emailUserNameConfiguration(this),
                    inputGatewayPatternConfiguration(this),
                    streetNameConfiguration(this),
                    MaximumNotifyDurationSeconds(this),
                    outputGatewayPatternConfiguration(this),
                    subjectTemplateConfiguration(this),
                    worksheetsSearchDirectoryConfiguration(this),
                    streetNameSearchTermConfiguration(this),
                    postCodeSearchTermConfiguration(this),
                    startUrlConfiguration(this),
                    waitDurationSecondsConfiguration(this)
                ).mapLeft { MainConfigurationError }.flatMap { configuration ->
                    createApplicationFactory(configuration).mapLeft { MainFactoryError }.flatMap { factory ->
                        factory.notifyOfNextServiceDate.execute().mapLeft { MainExecutionError }
                    }
                }
            }
        }

    companion object {
        private const val PROPERTIES_PATH_PROPERTY_NAME = "PROPERTIES_PATH"

        private fun propertyPathProperty(properties: Properties): Either<MainNoPropertyPathPropertyError, String> =
            when (properties.containsKey(PROPERTIES_PATH_PROPERTY_NAME)) {
                true -> right(properties.getProperty(PROPERTIES_PATH_PROPERTY_NAME))
                false -> left(MainNoPropertyPathPropertyError)
            }

        private fun applicationPropertiesFile(propertiesPath: String): Either<MainNoPropertiesFileError, File> =
            File(propertiesPath).run {
                when (exists()) {
                    true -> right(this)
                    false -> left(MainNoPropertiesFileError)
                }
            }

        private fun applicationPropertiesFileIsNotDirectory(propertiesFile: File): Either<MainPropertiesFileIsDirectoryError, File> =
            propertiesFile.run {
                when (!isDirectory) {
                    true -> right(this)
                    false -> left(MainPropertiesFileIsDirectoryError)
                }
            }

        private fun loadApplicationProperties(propertiesFile: File): Either<MainRefuseRecyclingDatesError, Properties> =
            try {
                propertiesFile
                    .inputStream()
                    .use { fis ->
                        try {
                            right(Properties().apply { load(fis) })
                        } catch (iae: IllegalArgumentException) {
                            left(MainMalformedPropertiesFileError)
                        }
                    }
            } catch (ioe: IOException) {
                left(MainPropertiesFileLoadError)
            }

        @JvmStatic
        fun main(args: Array<String>): Unit = Main().execute.fold({}, {})
    }
}
