package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.preferences.Environments

object TestResources {
    fun loadEnvironments(path: String): Environments {
        val environments = ClassLoader
            .getSystemClassLoader()
            .getResource("preferences/$path")
            .readText()

        return Environments.load(environments)
    }

    fun loadConfiguration(path: String): String {
        return ClassLoader.getSystemClassLoader().getResource("configuration/$path").readText()
    }
}