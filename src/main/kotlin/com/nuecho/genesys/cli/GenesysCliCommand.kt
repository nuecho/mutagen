package com.nuecho.genesys.cli

import picocli.CommandLine

abstract class GenesysCliCommand {
    @CommandLine.Option(names = ["-h", "--help"],
            usageHelp = true,
            description = ["display a help message"])
    private var usageRequested = false
}