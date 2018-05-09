package com.nuecho.genesys.cli.commands.audio

import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.commands.audio.export.AudioExportCommand
import com.nuecho.genesys.cli.commands.audio.import.AudioImportCommand
import picocli.CommandLine

@CommandLine.Command(
    name = "audio",
    description = ["GAX ARM tool"],
    subcommands = [AudioImportCommand::class, AudioExportCommand::class]
)
class Audio : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute() {
        CommandLine.usage(this, System.out)
    }

    override fun getGenesysCli(): GenesysCli = genesysCli!!
}
