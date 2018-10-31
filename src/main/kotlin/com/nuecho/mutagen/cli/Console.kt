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

package com.nuecho.mutagen.cli

import com.nuecho.mutagen.cli.preferences.SecurePassword
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
