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

package com.nuecho.mutagen.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.Level.WARN
import ch.qos.logback.classic.Logger
import mu.KotlinLogging
import org.slf4j.LoggerFactory.getLogger

object Logging {
    private val defaultLogLevel = WARN
    private val logger = KotlinLogging.logger {}

    // Log Level
    fun setToDefault() = setLogLevel(defaultLogLevel)

    fun setToInfo() = setLogLevel(INFO)

    fun setToDebug() = setLogLevel(DEBUG)

    private fun setLogLevel(level: Level) {
        val root = getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        root.level = level
    }

    // Logging
    fun warn(message: () -> Any?) {
        logger.warn(message)
    }

    fun info(message: () -> Any?) {
        logger.info(message)
    }

    fun debug(message: () -> Any?) {
        logger.debug(message)
    }
}

fun String.pluralize(count: Int, plural: String? = null): String? =
    if (count > 1) plural ?: this + 's' else this
