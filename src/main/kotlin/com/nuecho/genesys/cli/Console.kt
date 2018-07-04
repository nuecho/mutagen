package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.commands.config.import.NO
import com.nuecho.genesys.cli.commands.config.import.YES

object Console {
    fun confirm(): Boolean {
        var confirmed: String
        do {
            print("Please confirm [$YES|$NO]: ")
            confirmed = readLine()?.toLowerCase() ?: ""
        } while (confirmed != YES && confirmed != NO)

        return confirmed == YES
    }

    fun promptForPassword(): String {
        Logging.debug { "Password not found in environment. Prompting." }
        val console = System.console() ?: throw IllegalStateException(
            """
            |Can't prompt for password process not attached to console.
            |Either add password in your environment file or use `mutagen set-password`
            |to store an encrypted version of it.
            """.trimMargin()
        )

        return String(console.readPassword("Password: "))
    }
}
