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

package com.nuecho.mutagen.cli.commands.audio

import com.nuecho.mutagen.cli.MutagenCli
import com.nuecho.mutagen.cli.commands.MutagenCliCommand
import com.nuecho.mutagen.cli.commands.audio.export.AudioExportCommand
import com.nuecho.mutagen.cli.commands.audio.import.AudioImportCommand
import picocli.CommandLine

@CommandLine.Command(
    name = "audio",
    description = ["GAX ARM tool"],
    subcommands = [AudioImportCommand::class, AudioExportCommand::class]
)
class Audio : MutagenCliCommand() {
    @CommandLine.ParentCommand
    private var mutagenCli: MutagenCli? = null

    override fun execute(): Int {
        CommandLine.usage(this, System.out)

        return 0
    }

    override fun getMutagenCli(): MutagenCli = mutagenCli!!
}
