package com.nuecho.genesys.cli.models.configuration

object ConfigurationTestData {

    fun defaultProperties(): CategorizedProperties? =
        mapOf(
            "section" to mapOf(
                "number" to ConfigurationObjectMocks.NUMBER,
                "string" to "def"
            )
        )
}
