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

package com.nuecho.mutagen.cli.services

import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectNotFoundException
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference

fun <T : ICfgObject> IConfService.retrieveObject(reference: ConfigurationObjectReference<T>): T? {
    @Suppress("UNCHECKED_CAST")
    if (ConfigurationObjectRepository.contains(reference)) return ConfigurationObjectRepository[reference] as T

    val query = try {
        reference.toQuery(this)
    } catch (_: ConfigurationObjectNotFoundException) {
        return null
    }
    return retrieveObject(reference.cfgObjectClass, query)
}

fun IConfService.getObjectDbid(reference: ConfigurationObjectReference<*>?): Int? {
    if (reference == null) return null
    val dbid = retrieveObject(reference)?.objectDbid ?: 0
    if (dbid != 0) return dbid
    return null
}
