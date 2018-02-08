package com.nuecho.genesys.cli.preferences

import java.io.File
import java.io.FileNotFoundException

object Preferences {
    const val DEFAULT_ENVIRONMENT = "default"

    private const val PREFERENCES_DIRECTORY_NAME = ".mutagen"
    private const val ENVIRONMENTS_FILENAME = "environments.yml"

    private val defaultPreferencesDirectory = File(System.getProperty("user.home"), PREFERENCES_DIRECTORY_NAME)
    private val defaultEnvironmentsFile = File(defaultPreferencesDirectory, ENVIRONMENTS_FILENAME)

    fun loadEnvironment(environment: String = DEFAULT_ENVIRONMENT, environmentsFile: File = defaultEnvironmentsFile):
            Environment = loadEnvironments(environmentsFile)[environment]
            ?: throw IllegalArgumentException("Environment ($environment) does not exists in your environments file")

    private fun loadEnvironments(environmentsFile: File): Environments =
            if (!environmentsFile.exists() || !environmentsFile.isFile)
                throw FileNotFoundException("Cannot load environments file ($environmentsFile)")
            else Environments.load(environmentsFile)
}