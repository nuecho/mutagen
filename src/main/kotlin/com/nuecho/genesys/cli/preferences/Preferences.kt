package com.nuecho.genesys.cli.preferences

import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.preferences.environment.Environment
import com.nuecho.genesys.cli.preferences.environment.Environments
import java.io.Console
import java.io.File
import java.io.FileNotFoundException

object Preferences {
    const val DEFAULT_ENVIRONMENT = "default"

    const val WORKING_DIRECTORY_VARIABLE = "user.dir"
    const val CUSTOM_HOME_VARIABLE = "MUTAGEN_HOME"
    const val USER_PROFILE_VARIABLE = "USERPROFILE"
    const val HOME_VARIABLE = "HOME"
    const val PREFERENCES_DIRECTORY_NAME = ".mutagen"
    private const val ENVIRONMENTS_FILENAME = "environments.yml"

    fun loadEnvironment(
        environmentName: String = DEFAULT_ENVIRONMENT,
        environmentsFile: File? = null
    ): Environment {
        val environmentVariables = System.getenv().toMutableMap()
        environmentVariables[WORKING_DIRECTORY_VARIABLE] = System.getProperty(WORKING_DIRECTORY_VARIABLE)

        val effectiveEnvironmentsFile = environmentsFile ?: findEnvironmentFile(environmentVariables)
        info { "Loading environment file ($effectiveEnvironmentsFile)" }

        val environment = Environments.load(effectiveEnvironmentsFile)[environmentName]
                ?: throw IllegalArgumentException("Environment ($environmentName) does not exists")
        environment.password = environment.password ?: promptForPassword()

        return environment
    }

    internal fun findEnvironmentFile(environmentVariables: Map<String?, String?>): File {
        mapOf(
            WORKING_DIRECTORY_VARIABLE to false,
            CUSTOM_HOME_VARIABLE to false,
            HOME_VARIABLE to true,
            USER_PROFILE_VARIABLE to true
        ).forEach {
            val (environmentPathVariable, appendPreferenceDirectory) = it
            val environmentPath = environmentVariables[environmentPathVariable]

            if (environmentPath == null) {
                debug { "$environmentPathVariable is not defined." }
                return@forEach
            }

            val environmentDirectory =
                if (appendPreferenceDirectory) File(environmentPath, PREFERENCES_DIRECTORY_NAME)
                else File(environmentPath)

            val environmentFile = File(environmentDirectory, ENVIRONMENTS_FILENAME)

            if (environmentFile.exists()) return environmentFile
            debug { "No environment file found in $environmentPathVariable (${environmentFile.parent})" }
        }

        throw FileNotFoundException("Cannot find environment file.")
    }

    fun promptForPassword(): String {
        debug { "Password not found in environment. Prompting." }
        val console: Console? = System.console()
        when (console) {
            null ->
                //In this case, the JVM is not attached to the console so we need to bail out
                throw IllegalStateException("Process not attached to console.")
            else ->
                return String(console.readPassword("Password: "))
        }
    }
}
