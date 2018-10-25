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

package com.nuecho.mutagen.cli.core

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
