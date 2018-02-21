package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import io.kotlintest.matchers.contain
import io.kotlintest.matchers.containsAll
import io.kotlintest.matchers.include
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNot
import io.kotlintest.matchers.startWith
import io.mockk.every
import io.mockk.spyk

private const val DEBUG_LOG_ENTRY = "This is a debug log entry."
private const val INFO_LOG_ENTRY = "This is an info log entry."

class GenesysCliTest : GenesysCliCommandTest() {
    init {
        "executing GenesysCli with no argument should print usage" {
            val output = execute()
            output should include(SYNOPSIS)
            output should include(FOOTER)
            output should include(EXTRA_FOOTER)
            output should include(BANNER)
        }

        "executing GenesysCli with -h argument should print usage" {
            val output = execute("-h")
            output should include(SYNOPSIS)
            output should include(FOOTER)
            output shouldNot include(EXTRA_FOOTER)
            output shouldNot include(BANNER)
        }

        "executing GenesysCli with -v argument should print version" {
            val expectedOutput = "mutagen version 0.0.0"

            val output = execute("-v")
            output should startWith(expectedOutput)
        }

        "executing GenesysCli without --stacktrace should print only print error message" {
            val message = "An error occured."
            val output = testException(message)
            output.trim() shouldBe message
        }

        "executing GenesysCli with --stacktrace should print the whole stacktrace" {
            val message = "An error occured."
            val output = testException(message, "--stacktrace")
            output should startWith("java.lang.RuntimeException: $message")
        }

        "executing GenesysCli with --info should print info traces" {
            val output = testLogging("--info")
            output should contain(INFO_LOG_ENTRY)
            output shouldNot contain(DEBUG_LOG_ENTRY)
        }

        "executing GenesysCli with --debug should print info and debug traces" {
            val output = testLogging("--debug")
            output should containsAll(DEBUG_LOG_ENTRY, INFO_LOG_ENTRY)
        }

        "executing GenesysCli with --debug and --info should print info and debug traces" {
            val output = testLogging("--debug", "--info")
            output should containsAll(DEBUG_LOG_ENTRY, INFO_LOG_ENTRY)
        }
    }

    private fun testException(message: String, vararg args: String): String {
        val command = spyk(GenesysCli())

        every {
            command.run()
        } throws RuntimeException(message)

        val (returnCode, output) = captureOutput { GenesysCli.execute(command, *args) }
        returnCode shouldBe 1
        return output
    }

    private fun testLogging(vararg args: String): List<String> {
        val command = spyk(GenesysCli())

        every {
            command.execute()
        } answers {
            debug { DEBUG_LOG_ENTRY }
            info { INFO_LOG_ENTRY }
        }

        val (returnCode, output) = captureOutput { GenesysCli.execute(command, *args) }
        returnCode shouldBe 0
        return output.split(System.lineSeparator())
    }
}
