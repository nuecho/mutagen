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

package com.nuecho.mutagen.cli.commands.config

import com.nuecho.mutagen.cli.CliOutputCaptureWrapper.execute
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

private const val USAGE_PREFIX = "Usage: config [-?]"

class ConfigTest {
    @Test
    fun `executing Config with no argument should print usage`() {
        val output = execute("config")
        assertThat(output, startsWith(USAGE_PREFIX))
    }

    @Test
    fun `executing Config with -h argument should print usage`() {
        val output = execute("config", "-h")
        assertThat(output, startsWith(USAGE_PREFIX))
    }
}
