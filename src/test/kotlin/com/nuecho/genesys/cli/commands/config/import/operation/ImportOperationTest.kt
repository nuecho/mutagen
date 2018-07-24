package com.nuecho.genesys.cli.commands.config.import.operation

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.captureOutput
import com.nuecho.genesys.cli.TestResources
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

private const val RESOURCES_BASEPATH = "commands/config/import/operation"

@Suppress("UnnecessaryAbstractClass")
abstract class ImportOperationTest(
    private val operation: ImportOperation
) {
    @Test
    fun `print(detailed=true) should print detailed description of the operation`() {
        testPrint(true)
    }

    @Test
    fun `print(detailed=false) should print simplified description of the operation`() {
        testPrint(false)
    }

    private fun testPrint(detailed: Boolean) {
        val (_, actualOutput) = captureOutput { operation.print(detailed) }

        val filePrefix = operation::class.simpleName!!.toLowerCase().replace("operation", "-operation")
        val fileSuffix = if (detailed) "detailed" else "simple"
        val expectedOutput = TestResources.getTestResource("$RESOURCES_BASEPATH/$filePrefix-$fileSuffix.txt").readText()

        MatcherAssert.assertThat(actualOutput, Matchers.equalTo(expectedOutput))
    }
}
