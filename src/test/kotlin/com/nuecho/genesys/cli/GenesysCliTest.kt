package com.nuecho.genesys.cli

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.captureOutput
import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import com.nuecho.genesys.cli.Logging.debug
import com.nuecho.genesys.cli.Logging.info
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val DEBUG_LOG_ENTRY = "This is a debug log entry."
private const val INFO_LOG_ENTRY = "This is an info log entry."

class GenesysCliTest {

    @Test
    fun `executing GenesysCli with no argument should print usage`() {
        val output = execute()
        assertTrue(output.contains(SYNOPSIS))
        assertTrue(output.contains(FOOTER))
        assertTrue(output.contains(EXTRA_FOOTER))
        assertTrue(output.contains(BANNER.lfToPlatformEol()))
    }

    @Test
    fun `executing GenesysCli with -h argument should print usage`() {
        val output = execute("-h")
        assertTrue(output.contains(SYNOPSIS))
        assertTrue(output.contains(FOOTER))
        assertFalse(output.contains(EXTRA_FOOTER))
        assertFalse(output.contains(BANNER.lfToPlatformEol()))
    }

    @Test
    fun `executing GenesysCli with -v argument should print version`() {
        val expectedOutput = "mutagen version 0.0.0"

        val output = execute("-v")
        assertTrue(output.startsWith(expectedOutput))
    }

    @Test
    fun `executing GenesysCli without --stacktrace should print only print error message`() {
        val message = "An error occured."
        val output = testException(message)
        assertEquals(message, output.trim())
    }

    @Test
    fun `executing GenesysCli with --stacktrace should print the whole stacktrace`() {
        val message = "An error occured."
        val output = testException(message, "--stacktrace")
        assertTrue(output.startsWith("java.lang.RuntimeException: $message"))
    }

    @Test
    fun `executing GenesysCli with --info should print info traces`() {
        val output = testLogging("--info")
        assertTrue(output.contains(INFO_LOG_ENTRY))
        assertFalse(output.contains(DEBUG_LOG_ENTRY))
    }

    @Test
    fun `executing GenesysCli with --debug should print info and debug traces`() {
        val output = testLogging("--debug")
        assertTrue(output.containsAll(listOf(INFO_LOG_ENTRY, DEBUG_LOG_ENTRY)))
    }

    @Test
    fun `executing GenesysCli with --debug and --info should print info and debug traces`() {
        val output = testLogging("--debug", "--info")
        assertTrue(output.containsAll(listOf(INFO_LOG_ENTRY, DEBUG_LOG_ENTRY)))
    }
}

private fun testException(message: String, vararg args: String): String {
    val command = spyk(GenesysCli())

    every {
        command.call()
    } throws RuntimeException(message)

    val (returnCode, output) = captureOutput { GenesysCli.execute(command, *args) }
    assertEquals(1, returnCode)
    return output
}

private fun testLogging(vararg args: String): List<String> {
    val command = spyk(GenesysCli())

    every {
        command.execute()
    } answers {
        debug { DEBUG_LOG_ENTRY }
        info { INFO_LOG_ENTRY }
        0
    }

    val (returnCode, output) = captureOutput { GenesysCli.execute(command, *args) }
    assertEquals(0, returnCode)
    return output.split(System.lineSeparator())
}

private fun String.lfToPlatformEol(): String = this.replace("\n", System.lineSeparator())
