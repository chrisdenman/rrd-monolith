package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import arrow.core.flatMap
import java.io.File
import java.io.IOException
import java.util.Properties

sealed class RefuseRecyclingDatesException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable() {
    object NoPropertyPathPropertyDefinedException : RefuseRecyclingDatesException()
    data class PropertiesFileDoesNotExistException(override val message: String) : RefuseRecyclingDatesException()
    data class PropertiesFileIsDirectoryException(override val message: String) : RefuseRecyclingDatesException()
    data class MalformedPropertiesFileException(
        override val message: String,
        override val cause: Throwable?
    ) : RefuseRecyclingDatesException()
    data class PropertiesFileLoadException(
        override val message: String,
        override val cause: Throwable
    ) : RefuseRecyclingDatesException()
    data class CouldNotConstructApplicationConfigurationException(
        override val cause: Throwable
    ) : RefuseRecyclingDatesException()

    object FactoryException : RefuseRecyclingDatesException()
    object ExecutionException : RefuseRecyclingDatesException()
}

typealias MainRefuseRecyclingDatesError = RefuseRecyclingDatesException
typealias NoPropertyPathPropertyDefined = RefuseRecyclingDatesException.NoPropertyPathPropertyDefinedException
typealias PropertiesFileDoesNotExist = RefuseRecyclingDatesException.PropertiesFileDoesNotExistException
typealias PropertiesFileIsDirectory = RefuseRecyclingDatesException.PropertiesFileIsDirectoryException
typealias MalformedPropertiesFile = RefuseRecyclingDatesException.MalformedPropertiesFileException
typealias UnableToLoadPropertiesFile = RefuseRecyclingDatesException.PropertiesFileLoadException
typealias CouldNotConstructApplicationConfiguration = RefuseRecyclingDatesException.CouldNotConstructApplicationConfigurationException

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
                )
                    .mapLeft { error -> CouldNotConstructApplicationConfiguration(cause = error) }
                    .flatMap { configuration ->
                        createApplicationFactory(configuration)
                            .mapLeft { MainFactoryError }.flatMap { factory ->
                                factory.notifyOfNextServiceDate.execute().mapLeft { MainExecutionError }
                            }
                    }
            }
        }

    companion object {
        private const val PROPERTIES_PATH_PROPERTY_NAME = "PROPERTIES_PATH"

        private fun propertyPathProperty(properties: Properties): Either<NoPropertyPathPropertyDefined, String> =
            when (properties.containsKey(PROPERTIES_PATH_PROPERTY_NAME)) {
                true -> right(properties.getProperty(PROPERTIES_PATH_PROPERTY_NAME))
                false -> left(NoPropertyPathPropertyDefined)
            }

        private fun applicationPropertiesFile(propertiesPath: String): Either<PropertiesFileDoesNotExist, File> =
            File(propertiesPath).run {
                when (exists()) {
                    true -> right(this)
                    false -> left(
                        PropertiesFileDoesNotExist("The properties file path '$propertiesPath' does not exist.")
                    )
                }
            }

        private fun applicationPropertiesFileIsNotDirectory(propertiesFile: File): Either<PropertiesFileIsDirectory, File> =
            propertiesFile.run {
                when (!isDirectory) {
                    true -> right(this)
                    false -> left(
                        PropertiesFileIsDirectory(
                            "The properties file path '${propertiesFile.absolutePath}' is a directory."
                        )
                    )
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
                            left(
                                MalformedPropertiesFile(
                                    "The properties file '${propertiesFile.absolutePath}' is malformed.",
                                    iae
                                )
                            )
                        }
                    }
            } catch (ioe: IOException) {
                left(
                    UnableToLoadPropertiesFile(
                        "Could not load the properties file '${propertiesFile.absolutePath}'.",
                        ioe
                    )
                )
            }

        @JvmStatic
        fun main(args: Array<String>): Unit = Main().execute.fold({}, {})
    }
}
