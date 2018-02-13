package com.nuecho.genesys.cli

import io.kotlintest.specs.StringSpec
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

abstract class GenesysCliCommandTest : StringSpec() {
    internal val usagePrefix = "Usage: mutagen [-?disv] [-e=<environmentName>]"

    protected fun <T> captureOutput(command: () -> T): Pair<T, String> {
        val byteOutput = ByteArrayOutputStream()
        val printStream = PrintStream(byteOutput)

        System.setOut(printStream)
        System.setErr(printStream)

        val returnValue = command()

        System.setOut(System.out)
        System.setErr(System.err)

        return Pair(returnValue, String(byteOutput.toByteArray()))
    }

    protected fun execute(vararg args: String): String {
        val (_, output) = captureOutput { CommandLine.run(GenesysCli(), System.out, *args) }
        return output
    }
}
