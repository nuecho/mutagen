package com.nuecho.genesys.cli.models.configuration

object ConfigurationTestData {

    fun defaultProperties(): Map<String, Any>? =
        mapOf(
            "number" to ConfigurationObjectMocks.NUMBER,
            "string" to "abc",
            "bytes" to "abc".toByteArray(),
            "subProperties" to mapOf(
                "subNumber" to ConfigurationObjectMocks.SUB_NUMBER,
                "subString" to "def",
                "subBytes" to "def".toByteArray()
            )
        )
}
