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
import com.genesyslab.platform.applicationblocks.com.objects.CfgFolder
import com.genesyslab.platform.applicationblocks.com.queries.CfgFolderQuery
import com.nuecho.mutagen.cli.Logging.debug
import com.nuecho.mutagen.cli.core.MetricNames.CONFIG_PREFETCH
import com.nuecho.mutagen.cli.core.Metrics.time
import com.nuecho.mutagen.cli.getReference
import com.nuecho.mutagen.cli.models.configuration.reference.ConfigurationObjectReference

class ConfigurationObjectRepository {
    private val configurationObjects = HashMap<ConfigurationObjectReference<*>, ICfgObject>()

    fun contains(reference: ConfigurationObjectReference<*>) = configurationObjects.containsKey(reference)

    operator fun get(reference: ConfigurationObjectReference<*>) = configurationObjects[reference]

    operator fun set(reference: ConfigurationObjectReference<*>, cfgObject: ICfgObject) {
        configurationObjects[reference] = cfgObject
    }

    fun prefetchConfigurationObjects(confService: IConfService) {
        debug { "Prefetching configuration objects." }

        debug { "Prefetching CfgFolder objects." }

        time(CONFIG_PREFETCH) {
            confService.retrieveMultipleObjects(CfgFolder::class.java, CfgFolderQuery()).forEach {
                val reference = it.getReference()
                debug { "Prefetching '$reference'." }
                configurationObjects[reference] = it
            }
        }

        debug { "Prefetched ${configurationObjects.size} configurations objects." }
    }
}
