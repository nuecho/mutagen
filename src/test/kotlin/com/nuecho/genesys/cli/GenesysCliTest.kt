package com.nuecho.genesys.cli

import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.startWith
import io.kotlintest.mock.mock
import org.mockito.BDDMockito.given

class GenesysCliTest : CommandTest() {
    private var command = GenesysCli()

    init {
        val usagePrefix = "Usage: mutagen"

        "executing GenesysCli with no argument should print usage" {
            val output = execute()
            output should startWith(usagePrefix)
        }

        "executing GenesysCli with -h argument should print usage" {
            val output = execute("-h")
            output should startWith(usagePrefix)
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
    }

    private fun testException(message: String, vararg args: String): String {
        command = mock()
        given(command.run()).willThrow(RuntimeException(message))

        val (returnCode, output) = captureOutput { GenesysCli.execute(command, *args) }

        returnCode shouldBe 1
        return output
    }

    override fun createCommand(): Runnable {
        return command
    }
}
