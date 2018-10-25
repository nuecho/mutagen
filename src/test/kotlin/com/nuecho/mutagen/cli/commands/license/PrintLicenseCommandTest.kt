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

package com.nuecho.mutagen.cli.commands.license

import com.nuecho.mutagen.cli.CliOutputCaptureWrapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PrintLicenseCommandTest {
    @Test
    fun `executing Status with no arguments should print usage`() {
        val expectedOutput = ClassLoader.getSystemClassLoader().getResource(PrintLicenseCommand.LICENSE_FILENAME).readText()
        val output = CliOutputCaptureWrapper.execute("license")
        Assertions.assertEquals(expectedOutput, output)
    }
}
