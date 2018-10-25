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

package com.nuecho.mutagen.cli.core

object MetricNames {
    private const val COMMAND = "command"
    const val COMMAND_EXECUTE = "$COMMAND.execute"

    private const val SERVICE = "service"
    const val SERVICE_OPEN = "$SERVICE.open"
    const val SERVICE_CLOSE = "$SERVICE.close"

    private const val CONFIG = "config"
    const val CONFIG_PREFETCH = "$CONFIG.prefetch"
    const val CONFIG_EXPORT = "$CONFIG.export"
    const val CONFIG_EXPORT_RETRIEVE = "$CONFIG_EXPORT.retrieve"
    const val CONFIG_EXPORT_PROCESS = "$CONFIG_EXPORT.process"
}
