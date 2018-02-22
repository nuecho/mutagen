package com.nuecho.genesys.cli.commands.config

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec

private const val USAGE_PREFIX = "Usage: config [-?]"

class ConfigTest : StringSpec() {
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
