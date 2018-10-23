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

package com.nuecho.genesys.cli.models.configuration.reference

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgQuery
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.genesys.cli.core.setBuilder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.getCfgObjectType
import com.nuecho.genesys.cli.toShortName

abstract class ConfigurationObjectReference<T : ICfgObject>(
    @get:JsonIgnore
    val cfgObjectClass: Class<T>
) : Comparable<ConfigurationObjectReference<*>> {

    abstract fun toQuery(service: IConfService): ICfgQuery<T>

    @JsonIgnore
    fun getCfgObjectType() = getCfgObjectType(cfgObjectClass)

    override fun compareTo(other: ConfigurationObjectReference<*>) =
        getCfgObjectType().name().compareTo(other.getCfgObjectType().name())

    fun toConsoleString(): String = "${getCfgObjectType().toShortName()} [$this]"
}

fun referenceSetBuilder() = setBuilder<ConfigurationObjectReference<*>>()
