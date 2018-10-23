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

package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.VersionProvider
import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.preferences.environment.Environment
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

private const val UNKNOWN_HOST = "unknown"

data class Metadata(
    val formatName: String,
    val formatVersion: String,
    val mutagenVersion: String = VersionProvider.getVersionNumber(),
    val date: String = now().format(ISO_DATE_TIME),
    val user: String = System.getProperty("user.name"),
    val host: String = UNKNOWN_HOST
) {
    companion object {
        fun create(format: ExportFormat, environment: Environment) = Metadata(
            formatName = format.name,
            formatVersion = format.version,
            host = environment.host
        )
    }
}
