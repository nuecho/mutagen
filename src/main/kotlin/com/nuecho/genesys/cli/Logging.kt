package com.nuecho.genesys.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory.getLogger

object Logging {
    fun setToInfo() {
        setLogLevel(INFO)
    }

    fun setToDebug() {
        setLogLevel(DEBUG)
    }

    private fun setLogLevel(level: Level) {
        val root = getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        root.level = level
    }
}
