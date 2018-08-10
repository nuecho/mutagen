package com.nuecho.genesys.cli.core

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class SetBuilderTest {
    @Test
    fun `add(item) should add only non-null items`() {
        val nullString: String? = null
        val actual = setBuilder<String>().add("a").add(nullString).add("b").toSet()
        assertThat(actual, equalTo(setOf("a", "b")))
    }

    @Test
    fun `add(items) should add only non-null iterables`() {
        val nullList: List<String>? = null
        val actual = setBuilder<String>().add(listOf("a", "b")).add(nullList).toSet()
        assertThat(actual, equalTo(setOf("a", "b")))
    }

    @Test
    fun `toSet() shoud return a flat list of all added items`() {
        val nullString: String? = null
        val nullList: List<String>? = null
        val actual = setBuilder<String>().add(listOf("a", "b")).add(nullList).add("c").add(nullString).toSet()
        assertThat(actual, equalTo(setOf("a", "b", "c")))
    }
}
