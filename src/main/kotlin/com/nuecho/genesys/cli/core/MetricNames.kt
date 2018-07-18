package com.nuecho.genesys.cli.core

object MetricNames {
    private const val COMMAND = "command"
    const val COMMAND_EXECUTE = "$COMMAND.execute"

    private const val SERVICE = "service"
    const val SERVICE_OPEN = "$SERVICE.open"
    const val SERVICE_CLOSE = "$SERVICE.close"

    private const val CONFIG = "config"
    const val CONFIG_PREFETCH = "$CONFIG.prefetch"
    const val CONFIG_EXPORT = "$CONFIG.export"
    const val CONFIG_EXPORT_RETRIEVE = "$CONFIG_EXPORT.retrieve"
    const val CONFIG_EXPORT_PROCESS = "$CONFIG_EXPORT.process"
}
