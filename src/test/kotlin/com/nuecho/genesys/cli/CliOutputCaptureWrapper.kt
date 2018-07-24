package com.nuecho.genesys.cli

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
        val (_, output) = captureOutput { CommandLine.call(GenesysCli(), System.out, *args) }
        return output
    }
}
