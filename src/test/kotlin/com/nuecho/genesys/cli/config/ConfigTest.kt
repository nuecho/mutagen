package com.nuecho.genesys.cli.config

import com.nuecho.genesys.cli.GenesysCliCommandTest
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith

private const val USAGE_PREFIX = "Usage: config [-?]"

class ConfigTest : GenesysCliCommandTest() {
    init {
        "executing Config with no argument should print usage" {
            val output = execute("config")
            output should startWith(USAGE_PREFIX)
        }

        "executing Config with -h argument should print usage" {
            val output = execute("config", "-h")
            output should startWith(USAGE_PREFIX)
        }
    }
}
