package com.nuecho.genesys.cli

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
