package com.nuecho.genesys.cli.preferences

import java.io.File
import java.io.FileNotFoundException

object Preferences {
    private const val PREFERENCES_DIRECTORY = ".gen-cli"
    private const val ENVIRONMENTS_FILENAME = "environments.yml"

    fun loadEnvironments(): Environments {
        val userDirectory = File(System.getProperty("user.home"))
        val preferencesDirectory = File(userDirectory, PREFERENCES_DIRECTORY)
        val environmentsFile = File(preferencesDirectory, ENVIRONMENTS_FILENAME)

        if (!environmentsFile.exists() || !environmentsFile.isFile) {
            throw FileNotFoundException("Cannot load environments file ($environmentsFile)")
        }

        return Environments.load(environmentsFile)
    }
}