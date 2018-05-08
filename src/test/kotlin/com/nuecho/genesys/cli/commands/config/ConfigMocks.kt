package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.commands.config.export.ExportFormat
import com.nuecho.genesys.cli.models.configuration.Metadata

object ConfigMocks {
    fun mockMetadata(format: ExportFormat) =
        Metadata(
            date = "now",
            formatName = format.name,
            formatVersion = "1.1.1",
            host = "localhost",
            mutagenVersion = "2.2.2",
            user = "mutagen"
        )
}
