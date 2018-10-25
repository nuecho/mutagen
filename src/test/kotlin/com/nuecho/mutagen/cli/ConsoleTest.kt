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

package com.nuecho.mutagen.cli

import io.mockk.every
import io.mockk.staticMockk
import io.mockk.use
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConsoleTest {
    @Test
    fun `confirm should return true when user enters y`() {
        staticMockk("kotlin.io.ConsoleKt").use {
            every { readLine() } returns "y" andThen "Y"
            MatcherAssert.assertThat(Console.confirm(), Matchers.`is`(true))
            MatcherAssert.assertThat(Console.confirm(), Matchers.`is`(true))
        }
    }

    @Test
    fun `confirm should return false when user enters n`() {
        staticMockk("kotlin.io.ConsoleKt").use {
            every { readLine() } returns "n" andThen "N"
            MatcherAssert.assertThat(Console.confirm(), Matchers.`is`(false))
            MatcherAssert.assertThat(Console.confirm(), Matchers.`is`(false))
        }
    }

    @Test
    fun `confirm should keep prompting until user enters a valid answer`() {
        staticMockk("kotlin.io.ConsoleKt").use {
            every { readLine() } returns "q" andThen "t" andThen "Y"
            MatcherAssert.assertThat(Console.confirm(), Matchers.`is`(true))
        }
    }

    @Test
    fun `calling promptPassword with no console attached should throw`() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            Console.promptForPassword()
        }
    }
}
