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
