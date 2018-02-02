package com.nuecho.genesys.cli

import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GenesysCliTest : StringSpec() {
    init {
        val usagePrefix = "Usage: gen"

        "executing GenesysCli with no argument should print usage" {
            val output = execute()
            output should startWith(usagePrefix)
        }

        "executing GenesysCli with -h argument should print usage" {
            val output = execute("-h")
            output should startWith(usagePrefix)
        }
    }

    private fun execute(vararg args: String): String {
        val byteOutput = ByteArrayOutputStream()
        val output = PrintStream(byteOutput)
        CommandLine.run(GenesysCli(output), output, *args)
        return String(byteOutput.toByteArray())
    }
}