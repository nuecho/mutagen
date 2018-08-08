package com.nuecho.genesys.cli

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
