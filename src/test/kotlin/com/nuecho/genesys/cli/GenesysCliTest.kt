package com.nuecho.genesys.cli

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class GenesysCliTest : StringSpec() {
    init {
        println("Start Kotlin UnitTest")

        "length should return size of string" {
            "hello".length shouldBe 5
        }

        GenesysCli.test()
    }
}