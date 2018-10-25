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

import org.fusesource.jansi.Ansi
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object CliOutputCaptureWrapper {
    fun <T> captureOutput(command: () -> T): Pair<T, String> {
        val byteOutput = ByteArrayOutputStream()
        val printStream = PrintStream(byteOutput)

        System.setOut(printStream)
        System.setErr(printStream)
        Ansi.setEnabled(false)

        val returnValue = command()

        Ansi.setEnabled(true)
        System.setOut(System.out)
        System.setErr(System.err)

        return Pair(returnValue, String(byteOutput.toByteArray()))
    }

    fun execute(vararg args: String): String {
        val (_, output) = captureOutput { CommandLine.call(MutagenCli(), System.out, *args) }
        return output
    }
}
