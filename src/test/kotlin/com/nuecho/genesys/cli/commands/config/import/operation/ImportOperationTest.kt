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
