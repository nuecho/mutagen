/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.genesys.cli

import picocli.CommandLine.IVersionProvider
import java.io.IOException
import java.net.URLClassLoader
import java.util.jar.Manifest

class VersionProvider : IVersionProvider {
    companion object {
        private const val DEFAULT_APPLICATION_NAME = "mutagen"
        private const val DEFAULT_VERSION = "0.0.0"

        // This only returns the manifest when ran from a packaged jar
        fun getManifest() =
            try {
                val classLoader = this::class.java.classLoader as URLClassLoader
                val manifestUrl = classLoader.findResource("META-INF/MANIFEST.MF")
                Manifest(manifestUrl.openStream())
            } catch (_: IOException) {
                null
            }

        fun getApplicationName(manifest: Manifest? = getManifest()) =
            manifest?.mainAttributes?.getValue("Application-Name")
                    ?: DEFAULT_APPLICATION_NAME

        fun getVersionNumber(manifest: Manifest? = getManifest()) =
            manifest?.mainAttributes?.getValue("Version") ?: DEFAULT_VERSION
    }

    override fun getVersion(): Array<String> {
        val manifest = getManifest()
        val name = getApplicationName(manifest)
        val version = getVersionNumber(manifest)
        return arrayOf("$name version $version")
    }
}
