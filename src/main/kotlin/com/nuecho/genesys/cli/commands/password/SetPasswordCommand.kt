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

package com.nuecho.genesys.cli.commands.password

import com.nuecho.genesys.cli.Console.promptForPassword
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.commands.GenesysCliCommand
import com.nuecho.genesys.cli.preferences.Passwords.encryptAndDigest
import com.nuecho.genesys.cli.preferences.Preferences
import com.nuecho.genesys.cli.preferences.SecurePassword
import com.nuecho.genesys.cli.preferences.environment.Environments
import picocli.CommandLine

@CommandLine.Command(
    name = "set-password",
    description = ["Save the password for your env encrypted in a not so secure way"]
)
class SetPasswordCommand : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var genesysCli: GenesysCli? = null

    override fun execute(): Int {
        val environmentName = getGenesysCli().environmentName
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

    override fun getGenesysCli() = genesysCli!!
}
