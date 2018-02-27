package com.nuecho.genesys.cli.core

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.OutputStream

private val objectMapper = jacksonObjectMapper()

fun defaultGenerator(outputStream: OutputStream = System.out): JsonGenerator = JsonFactory()
    .createGenerator(outputStream, JsonEncoding.UTF8)
    .setCodec(objectMapper)
    .setPrettyPrinter(DefaultPrettyPrinter())
