package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.preferences.environment.Environments
import mu.KotlinLogging
import java.io.Console
import java.io.File
import java.io.FileNotFoundException

object Preferences {
    const val DEFAULT_ENVIRONMENT = "default"

    private const val PREFERENCES_DIRECTORY_NAME = ".mutagen"
    private const val ENVIRONMENTS_FILENAME = "environments.yml"

    private val defaultPreferencesDirectory = File(System.getProperty("user.home"), PREFERENCES_DIRECTORY_NAME)
    private val defaultEnvironmentsFile = File(defaultPreferencesDirectory, ENVIRONMENTS_FILENAME)

    fun loadEnvironment(
        environment: String = DEFAULT_ENVIRONMENT,
        environmentsFile: File = defaultEnvironmentsFile
    ): Environment {

        val env = loadEnvironments(environmentsFile)[environment]
                ?: throw IllegalArgumentException("Environment ($environment) does not exists")

        env.password = env.password ?: promptForPassword()

        return env
    }

    private fun loadEnvironments(environmentsFile: File): Environments =
        if (!environmentsFile.exists() || !environmentsFile.isFile)
            throw FileNotFoundException("Cannot find environments file ($environmentsFile)")
        else Environments.load(environmentsFile)

    fun promptForPassword(): String {
        val logger = KotlinLogging.logger {}
        debug { "Password not found in environment. Prompting." }
        val console: Console? = System.console()
        when (console) {
            null ->
                //In this case, the JVM is not attached to the console so we need to bail out
                throw NullPointerException("Process not attached to console.")
            else ->
                return String(console.readPassword("Password: "))
        }
    }
}
