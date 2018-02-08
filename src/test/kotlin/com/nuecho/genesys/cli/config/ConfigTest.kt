package com.nuecho.genesys.cli.config

import com.nuecho.genesys.cli.CommandTest
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith

class ConfigTest : CommandTest() {
    init {
        val usagePrefix = "Usage: config"

        "executing Config with no argument should print usage" {
            val output = execute()
            output should startWith(usagePrefix)
        }

        "executing Config with -h argument should print usage" {
            val output = execute("-h")
            output should startWith(usagePrefix)
        }
    }

    override fun createCommand(): Runnable {
        return Config()
    }
}