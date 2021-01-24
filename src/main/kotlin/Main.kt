package uk.co.ceilingcat.rrd.monolith

import arrow.core.Either
import arrow.core.Either.Companion.conditionally
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import java.io.File
import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess

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

    data class CouldNotConstructApplicationFactoryException(
        override val cause: Throwable
    ) : RefuseRecyclingDatesException()

    object ExecutionFailureException : RefuseRecyclingDatesException()
}

typealias MainRefuseRecyclingDatesError = RefuseRecyclingDatesException
typealias NoPropertyPathPropertyDefined = RefuseRecyclingDatesException.NoPropertyPathPropertyDefinedException
typealias PropertiesFileDoesNotExist = RefuseRecyclingDatesException.PropertiesFileDoesNotExistException
typealias PropertiesFileIsDirectory = RefuseRecyclingDatesException.PropertiesFileIsDirectoryException
typealias MalformedPropertiesFile = RefuseRecyclingDatesException.MalformedPropertiesFileException
typealias UnableToLoadPropertiesFile = RefuseRecyclingDatesException.PropertiesFileLoadException
typealias CouldNotConstructApplicationConfiguration = RefuseRecyclingDatesException.CouldNotConstructApplicationConfigurationException
typealias CouldNotConstructApplicationFactory = RefuseRecyclingDatesException.CouldNotConstructApplicationFactoryException
typealias ExecutionFailure = RefuseRecyclingDatesException.ExecutionFailureException

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
                    .mapLeft { error ->
                        CouldNotConstructApplicationConfiguration(error)
                    }
                    .flatMap { configuration ->
                        createApplicationFactory(configuration)
                            .mapLeft { error ->
                                CouldNotConstructApplicationFactory(error)
                            }
                            .flatMap { factory ->
                                factory
                                    .notifyOfNextServiceDate
                                    .execute()
                                    .mapLeft { ExecutionFailure }
                            }
                    }
            }
        }

    companion object {
        private const val PROPERTIES_PATH_PROPERTY_NAME = "PROPERTIES_PATH"

        private fun propertyPathProperty(properties: Properties): Either<NoPropertyPathPropertyDefined, String> =
            conditionally(properties.containsKey(PROPERTIES_PATH_PROPERTY_NAME), { NoPropertyPathPropertyDefined }) {
                properties.getProperty(PROPERTIES_PATH_PROPERTY_NAME)
            }

        private fun applicationPropertiesFile(propertiesPath: String): Either<PropertiesFileDoesNotExist, File> =
            File(propertiesPath).run {
                conditionally(
                    exists(),
                    { PropertiesFileDoesNotExist("The properties file path '$propertiesPath' does not exist.") }
                ) { this }
            }

        private fun applicationPropertiesFileIsNotDirectory(propertiesFile: File): Either<PropertiesFileIsDirectory, File> =
            conditionally(
                !propertiesFile.isDirectory,
                {
                    PropertiesFileIsDirectory("The properties file path '${propertiesFile.absolutePath}' is a directory.")
                }
            ) { propertiesFile }

        private fun loadApplicationProperties(propertiesFile: File): Either<MainRefuseRecyclingDatesError, Properties> =
            try {
                propertiesFile
                    .inputStream()
                    .use { fis ->
                        try {
                            Properties().apply { load(fis) }.right()
                        } catch (iae: IllegalArgumentException) {
                            MalformedPropertiesFile(
                                "The properties file '${propertiesFile.absolutePath}' is malformed.",
                                iae
                            ).left()
                        }
                    }
            } catch (ioe: IOException) {
                UnableToLoadPropertiesFile(
                    "Could not load the properties file '${propertiesFile.absolutePath}'.",
                    ioe
                ).left()
            }

        @JvmStatic
        fun main(args: Array<String>): Unit = Main().execute.fold({ println(it) }, { exitProcess(0)})
    }
}
