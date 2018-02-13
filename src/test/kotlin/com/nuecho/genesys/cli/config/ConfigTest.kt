package com.nuecho.genesys.cli.config

import com.nuecho.genesys.cli.GenesysCliCommandTest
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith

class ConfigTest : GenesysCliCommandTest() {
    init {
        "executing Config with no argument should print usage" {
            val output = execute()
            output should startWith(usagePrefix)
        }

        "executing Config with -h argument should print usage" {
            val output = execute("-h")
            output should startWith(usagePrefix)
        }
    }
}
