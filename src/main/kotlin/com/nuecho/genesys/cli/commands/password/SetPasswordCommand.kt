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
