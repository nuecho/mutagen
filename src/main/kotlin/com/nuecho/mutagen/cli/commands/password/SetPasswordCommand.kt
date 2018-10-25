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

package com.nuecho.mutagen.cli.commands.password

import com.nuecho.mutagen.cli.Console.promptForPassword
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.MutagenCli
import com.nuecho.mutagen.cli.commands.MutagenCliCommand
import com.nuecho.mutagen.cli.preferences.Passwords.encryptAndDigest
import com.nuecho.mutagen.cli.preferences.Preferences
import com.nuecho.mutagen.cli.preferences.SecurePassword
import com.nuecho.mutagen.cli.preferences.environment.Environments
import picocli.CommandLine

@CommandLine.Command(
    name = "set-password",
    description = ["Save the password for your env encrypted in a not so secure way"]
)
class SetPasswordCommand : MutagenCliCommand() {
    @CommandLine.ParentCommand
    private var mutagenCli: MutagenCli? = null

    override fun execute(): Int {
        val environmentName = getMutagenCli().environmentName
        val environmentsFile = Preferences.findEnvironmentFile()
        val environments = Environments.load(environmentsFile)

        val environment = environments[environmentName]
                ?: throw IllegalArgumentException("Environment ($environmentName) does not exist")

        val password = promptForPassword()
        environment.password = SecurePassword(encryptAndDigest(password.value).toCharArray())

        debug { "Saving encrypted password to environment file." }
        environments.saveToFile(environmentsFile)

        return 0
    }

    override fun getMutagenCli() = mutagenCli!!
}
