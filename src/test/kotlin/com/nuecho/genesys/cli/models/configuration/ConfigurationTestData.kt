package com.nuecho.genesys.cli.models.configuration

object ConfigurationTestData {
    const val CATEGORIZED_PROPERTIES_NUMBER = 456
    const val CATEGORIZED_PROPERTIES_STRING = "def"

    fun defaultProperties(): CategorizedProperties? =
        mapOf(
            @Suppress("MagicNumber")
            "section" to mapOf(
                "number" to CATEGORIZED_PROPERTIES_NUMBER,
                "string" to CATEGORIZED_PROPERTIES_STRING
            )
        )
}
