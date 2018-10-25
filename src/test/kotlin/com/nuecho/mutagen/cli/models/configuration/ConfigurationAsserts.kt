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

package com.nuecho.mutagen.cli.models.configuration

import com.nuecho.mutagen.cli.TestResources.loadRawConfiguration
import com.nuecho.mutagen.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

object ConfigurationAsserts {
    private val mapper = defaultJsonObjectMapper()

    fun checkSerialization(configurationObject: Any, expectedFile: String) {
        val stringResult = mapper.writeValueAsString(configurationObject)
        val jsonResult = mapper.readTree(stringResult)
        assertThat(jsonResult, equalTo(loadRawConfiguration("models/configuration/$expectedFile.json")))
    }
}
