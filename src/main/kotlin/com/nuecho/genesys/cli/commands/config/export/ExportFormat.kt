package com.nuecho.genesys.cli.commands.config.export

enum class ExportFormat(val version: String) {
    RAW("1.0.0"), JSON("1.0.0"), COMPACT_JSON("1.0.0")
}
