package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.preferences.SecurePassword
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

const val YES: String = "y"
const val NO: String = "n"

object Console {
    fun confirm(): Boolean {
        var confirmed: String
        do {
            print("Please confirm [$YES|$NO]: ")
            confirmed = readLine()?.toLowerCase() ?: ""
        } while (confirmed != YES && confirmed != NO)

        return confirmed == YES
    }

    fun promptForPassword(): SecurePassword {
        Logging.debug { "Password not found in environment. Prompting." }
        val console = System.console() ?: throw IllegalStateException(
            """
            |Can't prompt for password process not attached to console.  You may:
            |  - Add password in your environment file
            |  - Pass it on standard input using `mutagen -p ...`
            |  - Use `mutagen set-password` to store an encrypted version of it.
            """.trimMargin()
        )

        return SecurePassword(console.readPassword("Password: "))
    }

    fun ansiPrint(string: String) {
        print(Ansi.ansi().render(string))
    }

    fun ansiPrintln(string: String) {
        println(Ansi.ansi().render(string))
    }

    fun enableAnsiMode() {
        AnsiConsole.systemInstall()
    }

    fun disableAnsiMode() {
        AnsiConsole.systemUninstall()
    }
}
