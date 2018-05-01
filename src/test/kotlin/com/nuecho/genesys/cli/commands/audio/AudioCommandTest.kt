package com.nuecho.genesys.cli.commands.audio

import com.nuecho.genesys.cli.CliOutputCaptureWrapper.execute
import io.kotlintest.matchers.should
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec

private const val USAGE_PREFIX = "Usage: audio [-?]"

class AudioCommandTest : StringSpec() {
    init {
        "executing Audio command with no argument should print usage" {
            val output = execute("audio")
            output should startWith(USAGE_PREFIX)
        }

        "executing Audio command with -h argument should print usage" {
            val output = execute("audio", "-h")
            output should startWith(USAGE_PREFIX)
        }
    }
}
