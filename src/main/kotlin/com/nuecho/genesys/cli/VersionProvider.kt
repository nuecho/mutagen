package com.nuecho.genesys.cli

import picocli.CommandLine.IVersionProvider
import java.io.IOException
import java.net.URLClassLoader
import java.util.jar.Manifest

class VersionProvider : IVersionProvider {
    companion object {
        const val DEFAULT_APPLICATION_NAME = "mutagen"
        const val DEFAULT_VERSION = "0.0.0"
    }

    override fun getVersion(): Array<String> {
        // This only returns the manifest when ran from a packaged jar
        val manifest = try {
            val cl = this::class.java.classLoader as URLClassLoader
            val url = cl.findResource("META-INF/MANIFEST.MF")
            Manifest(url.openStream())
        } catch (_: IOException) {
            null
        }

        val name = manifest?.mainAttributes?.getValue("Application-Name") ?: DEFAULT_APPLICATION_NAME
        val version = manifest?.mainAttributes?.getValue("Version") ?: DEFAULT_VERSION

        return arrayOf("$name version $version")
    }
}