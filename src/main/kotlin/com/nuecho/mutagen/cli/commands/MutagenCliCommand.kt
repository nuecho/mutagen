/*
 * Copyright (C) 2018 Nu Echo Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuecho.mutagen.cli.commands

import com.nuecho.mutagen.cli.Logging
import com.nuecho.mutagen.cli.MutagenCli
import com.nuecho.mutagen.cli.core.MetricNames.COMMAND_EXECUTE
import com.nuecho.mutagen.cli.core.Metrics
import com.nuecho.mutagen.cli.core.Metrics.time
import com.nuecho.mutagen.cli.preferences.SecurePassword
import picocli.CommandLine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Callable

abstract class MutagenCliCommand : Callable<Int> {
    @Suppress("unused")
    @CommandLine.Option(
        names = ["-?", "-h", "--help"],
        usageHelp = true,
        description = ["Show this help message."]
    )
    private var usageRequested = false

    internal var password: SecurePassword? = null

    abstract fun execute(): Int

    abstract fun getMutagenCli(): MutagenCli

    override fun call(): Int {
        val mutagenCli = getMutagenCli()

        if (mutagenCli.debug) {
            Logging.setToDebug()
        } else if (mutagenCli.info) {
            Logging.setToInfo()
        }

        if (mutagenCli.readPasswordFromStdin) {
            val passwordReader = BufferedReader(InputStreamReader(System.`in`))
            password = SecurePassword(passwordReader.readLine().toCharArray())
        }

        try {
            return time(COMMAND_EXECUTE) {
                execute()
            }
        } finally {
            mutagenCli.metricsFile?.let {
                it.parentFile?.mkdirs()
                it.outputStream().use { Metrics.output(it) }
            }
        }
    }
}
