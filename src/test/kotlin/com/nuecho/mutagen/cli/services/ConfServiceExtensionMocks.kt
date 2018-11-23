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

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgAgentLogin
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgCallingList
import com.genesyslab.platform.applicationblocks.com.objects.CfgCampaign
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNGroup
import com.genesyslab.platform.applicationblocks.com.objects.CfgEnumerator
import com.genesyslab.platform.applicationblocks.com.objects.CfgGVPReseller
import com.genesyslab.platform.applicationblocks.com.objects.CfgHost
import com.genesyslab.platform.applicationblocks.com.objects.CfgObjectiveTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.objects.CfgPhysicalSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgPlace
import com.genesyslab.platform.applicationblocks.com.objects.CfgScript
import com.genesyslab.platform.applicationblocks.com.objects.CfgSkill
import com.genesyslab.platform.applicationblocks.com.objects.CfgStatTable
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch
import com.genesyslab.platform.applicationblocks.com.objects.CfgTenant
import com.genesyslab.platform.applicationblocks.com.objects.CfgTimeZone
import com.genesyslab.platform.configuration.protocol.types.CfgAppType
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType
import com.genesyslab.platform.configuration.protocol.types.CfgScriptType
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_FOLDER_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_OBJECT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.DEFAULT_TENANT_DBID
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentGroup
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgAgentLogin
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgAppPrototype
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgApplication
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCallingList
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgCampaign
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgDN
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgDNGroup
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgEnumerator
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgFolder
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgGVPReseller
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgHost
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgObjectiveTable
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPerson
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPhysicalSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgPlace
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgScript
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSkill
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgStatTable
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgSwitch
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTenant
import com.nuecho.mutagen.cli.models.configuration.ConfigurationObjectMocks.mockCfgTimeZone
import io.mockk.every

object ConfServiceExtensionMocks {
    const val DEFAULT_NAME = "name"

    fun mockRetrieveApplication(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgApplication::class.java, any()) } answers {
            mockCfgApplication(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveScript(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID, type: CfgScriptType? = null) =
        every { service.retrieveObject(CfgScript::class.java, any()) } answers {
            mockCfgScript(name = DEFAULT_NAME, dbid = dbid, type = type)
        }

    fun mockRetrieveCallingList(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgCallingList::class.java, any()) } answers {
            mockCfgCallingList(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveObjectiveTable(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgObjectiveTable::class.java, any()) } answers {
            mockCfgObjectiveTable(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrievePhysicalSwitch(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgPhysicalSwitch::class.java, any()) } answers {
            mockCfgPhysicalSwitch(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrievePlace(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgPlace::class.java, any()) } answers {
            mockCfgPlace(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveSkill(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgSkill::class.java, any()) } answers {
            mockCfgSkill(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveAgentLogin(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgAgentLogin::class.java, any()) } answers {
            mockCfgAgentLogin(DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveAgentGroup(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgAgentGroup::class.java, any()) } answers {
            mockCfgAgentGroup(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveCampaign(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgCampaign::class.java, any()) } answers {
            mockCfgCampaign(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrievePerson(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgPerson::class.java, any()) } answers {
            mockCfgPerson(DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveDN(service: IConfService, switch: CfgSwitch, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgDN::class.java, any()) } answers {
            mockCfgDN(DEFAULT_NAME, dbid = dbid, switch = switch)
        }
    }

    fun mockRetrieveDNGroup(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgDNGroup::class.java, any()) } answers {
            mockCfgDNGroup(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveEnumerator(service: IConfService, name: String?, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgEnumerator::class.java, any()) } answers {
            mockCfgEnumerator(name = name, dbid = dbid)
        }
    }

    fun mockRetrieveStatTable(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgStatTable::class.java, any()) } answers {
            mockCfgStatTable(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveSwitch(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgSwitch::class.java, any()) } answers {
            mockCfgSwitch(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveTimeZone(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgTimeZone::class.java, any()) } answers {
            mockCfgTimeZone(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveTenant(service: IConfService, dbid: Int = DEFAULT_TENANT_DBID) {
        every { service.retrieveObject(CfgTenant::class.java, any()) } answers {
            mockCfgTenant(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveReseller(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) {
        every { service.retrieveObject(CfgGVPReseller::class.java, any()) } answers {
            mockCfgGVPReseller(name = DEFAULT_NAME, dbid = dbid)
        }
    }

    fun mockRetrieveHost(service: IConfService, dbid: Int = DEFAULT_OBJECT_DBID) =
        every { service.retrieveObject(CfgHost::class.java, any()) } answers {
            mockCfgHost(name = DEFAULT_NAME, dbid = dbid)
        }

    fun mockRetrieveAppPrototype(
        service: IConfService,
        dbid: Int = DEFAULT_OBJECT_DBID,
        type: CfgAppType? = null,
        version: String? = null
    ) {
        every { service.retrieveObject(CfgAppPrototype::class.java, any()) } answers {
            mockCfgAppPrototype(name = DEFAULT_NAME, dbid = dbid, type = type, version = version)
        }
    }

    fun mockRetrieveFolderByDbid(service: IConfService, dbid: Int = DEFAULT_FOLDER_DBID) {
        val folder = mockCfgFolder(name = DEFAULT_FOLDER, dbid = dbid)
        every { service.retrieveObject(CfgObjectType.CFGFolder, DEFAULT_FOLDER_DBID) } returns folder
    }
}
