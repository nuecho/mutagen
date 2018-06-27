package com.nuecho.genesys.cli.models.configuration

import com.nuecho.genesys.cli.TestResources
import com.nuecho.genesys.cli.core.defaultJsonObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

object ConfigurationAsserts {
    private val mapper = defaultJsonObjectMapper()

    fun checkSerialization(configurationObject: Any, expectedFile: String) {
        val stringResult = mapper.writeValueAsString(configurationObject)
        val jsonResult = mapper.readTree(stringResult)
        assertThat(jsonResult, equalTo(TestResources.loadRawConfiguration("models/configuration/$expectedFile.json")))
    }
}
