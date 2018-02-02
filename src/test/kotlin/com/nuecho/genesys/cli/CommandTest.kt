package com.nuecho.genesys.cli

import io.kotlintest.specs.StringSpec
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

abstract class CommandTest : StringSpec() {
    abstract fun createCommand(): Runnable

    protected fun execute(vararg args: String): String {
        val byteOutput = ByteArrayOutputStream()

        System.setOut(PrintStream(byteOutput))
        System.setErr(PrintStream(byteOutput))

        CommandLine.run(createCommand(), System.out, *args)

        System.setOut(System.out)
        System.setErr(System.err)

        return String(byteOutput.toByteArray())
    }
}