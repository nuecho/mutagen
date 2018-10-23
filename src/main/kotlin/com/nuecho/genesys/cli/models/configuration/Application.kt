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

package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.genesyslab.platform.applicationblocks.com.CfgObject
import com.genesyslab.platform.applicationblocks.com.ICfgObject
import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgAppPrototype
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication
import com.genesyslab.platform.applicationblocks.com.objects.CfgConnInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgPortInfo
import com.genesyslab.platform.applicationblocks.com.objects.CfgServer
import com.nuecho.genesys.cli.asBoolean
import com.nuecho.genesys.cli.getFolderReference
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.checkUnchangeableProperties
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setFolder
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.setProperty
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgAppComponentType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgFlag
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgHAType
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgObjectState
import com.nuecho.genesys.cli.models.configuration.ConfigurationObjects.toCfgTraceMode
import com.nuecho.genesys.cli.models.configuration.reference.AppPrototypeReference
import com.nuecho.genesys.cli.models.configuration.reference.ApplicationReference
import com.nuecho.genesys.cli.models.configuration.reference.ConfigurationObjectReference
import com.nuecho.genesys.cli.models.configuration.reference.FolderReference
import com.nuecho.genesys.cli.models.configuration.reference.HostReference
import com.nuecho.genesys.cli.models.configuration.reference.TenantReference
import com.nuecho.genesys.cli.models.configuration.reference.referenceSetBuilder
import com.nuecho.genesys.cli.services.ConfService
import com.nuecho.genesys.cli.services.getObjectDbid
import com.nuecho.genesys.cli.services.retrieveObject
import com.nuecho.genesys.cli.toShortName

/**
 *  Version and type properties are not defined.
 *  They could be set with mutagen, but
 *  - they cannot be set explicitly in GA nor GAX
 *  - specifying an appPrototype (Application Template) is mandatory on creation in GAX and GA
 *  - in GAX and GA, they are set automatically from the appPrototype
 */
@Suppress("DataClassContainsFunctions")
data class Application(
    val name: String,
    val appPrototype: AppPrototypeReference? = null,
    val appServers: List<ConnInfo>? = null,
    val autoRestart: Boolean? = null,
    val commandLine: String? = null,
    val commandLineArguments: String? = null,
    val componentType: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val flexibleProperties: CategorizedProperties? = null,
    @get:JsonProperty("isPrimary")
    val isPrimary: Boolean? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    val options: CategorizedProperties? = null,
    val password: String? = null,
    val portInfos: List<PortInfo>? = null,
    val redundancyType: String? = null,
    val serverInfo: Server? = null,
    val shutdownTimeout: Int? = null,
    val startupTimeout: Int? = null,
    val tenants: List<TenantReference>? = null,
    val workDirectory: String? = null,
    val state: String? = null,
    @JsonSerialize(using = CategorizedPropertiesSerializer::class)
    @JsonDeserialize(using = CategorizedPropertiesDeserializer::class)
    override val userProperties: CategorizedProperties? = null,
    override val folder: FolderReference? = null
) : ConfigurationObject {

    @get:JsonIgnore
    override val reference = ApplicationReference(name)

    // FIXME ignoring resources property

    // TODO one of the portInfos needs to have `default` as id.
    // we should consider having 2 fields: defaultPortInfos and additionalPortInfos

    constructor(cfgApplication: CfgApplication) : this(
        name = cfgApplication.name,
        appPrototype = if (cfgApplication.appPrototype != null) AppPrototypeReference(cfgApplication.appPrototype.name)
        else null,
        appServers = cfgApplication.appServers.map { ConnInfo(it) },
        autoRestart = cfgApplication.autoRestart?.asBoolean(),
        commandLine = cfgApplication.commandLine,
        commandLineArguments = cfgApplication.commandLineArguments,
        componentType = cfgApplication.componentType.toShortName(),
        flexibleProperties = cfgApplication.flexibleProperties?.asCategorizedProperties(),
        isPrimary = cfgApplication.isPrimary?.asBoolean(),
        options = cfgApplication.options?.asCategorizedProperties(),
        password = cfgApplication.password,
        portInfos = cfgApplication.portInfos.map { PortInfo(it) },
        redundancyType = cfgApplication.redundancyType.toShortName(),
        serverInfo = if (cfgApplication.serverInfo != null) Server(cfgApplication.serverInfo) else null,
        shutdownTimeout = cfgApplication.shutdownTimeout,
        startupTimeout = cfgApplication.startupTimeout,
        tenants = cfgApplication.tenants.map { TenantReference(it.name) },
        workDirectory = cfgApplication.workDirectory,
        state = cfgApplication.state?.toShortName(),
        userProperties = cfgApplication.userProperties?.asCategorizedProperties(),
        folder = cfgApplication.getFolderReference()
    )

    override fun createCfgObject(service: IConfService): CfgApplication {
        val cfgAppPrototype = service.retrieveObject(appPrototype!!) as CfgAppPrototype

        return updateCfgObject(service, CfgApplication(service)).also {
            setProperty("name", name, it)
            setProperty("type", cfgAppPrototype.type, it)
            setProperty("version", cfgAppPrototype.version, it)
            setFolder(folder, it)
        }
    }

    override fun updateCfgObject(service: IConfService, cfgObject: ICfgObject) =
        (cfgObject as CfgApplication).also {
            setProperty("appPrototypeDBID", service.getObjectDbid(appPrototype), it)
            setProperty("appServerDBIDs", appServers?.map { appServer -> appServer.toCfgConnInfo(it) }, it)
            setProperty("autoRestart", toCfgFlag(autoRestart), it)
            setProperty("commandLine", commandLine, it)
            setProperty("commandLineArguments", commandLineArguments, it)
            setProperty("componentType", toCfgAppComponentType(componentType), it)
            setProperty("flexibleProperties", ConfigurationObjects.toKeyValueCollection(flexibleProperties), it)
            setProperty("isPrimary", toCfgFlag(isPrimary), it)
            setProperty("options", ConfigurationObjects.toKeyValueCollection(options), it)
            setProperty("password", password, it)
            setProperty("portInfos", portInfos?.map { portInfo -> portInfo.toCfgPortInfo(it) }, it)
            setProperty("redundancyType", toCfgHAType(redundancyType), it)
            setProperty(
                "serverInfo",
                serverInfo?.toUpdatedCfgServer(service, it.serverInfo ?: CfgServer(service, it)),
                it
            )
            setProperty("shutdownTimeout", shutdownTimeout, it)
            setProperty("startupTimeout", startupTimeout, it)
            setProperty("state", toCfgObjectState(state), it)
            setProperty("tenantDBIDs", tenants?.map { service.getObjectDbid(it) }, it)
            setProperty("userProperties", ConfigurationObjects.toKeyValueCollection(userProperties), it)
            setProperty("workDirectory", workDirectory, it)
        }

    override fun cloneBare() = Application(
        name = name,
        appPrototype = appPrototype,
        autoRestart = autoRestart,
        redundancyType = redundancyType,
        workDirectory = workDirectory,
        commandLine = commandLine,
        startupTimeout = startupTimeout,
        shutdownTimeout = shutdownTimeout
    )

    override fun checkMandatoryProperties(configuration: Configuration, service: ConfService): Set<String> {
        val missingMandatoryProperties = mutableSetOf<String>()
        val prototype = getAppPrototype(configuration, service)

        // appPrototype is mandatory, because it is needed to set mandatory properties version and type
        appPrototype ?: missingMandatoryProperties.add(APP_PROTOTYPE)
        autoRestart ?: missingMandatoryProperties.add(AUTO_RESTART)
        redundancyType ?: missingMandatoryProperties.add(REDUNDANCY_TYPE)

        if (prototype?.type != null && isServer(prototype.type)) {
            commandLine ?: missingMandatoryProperties.add(COMMAND_LINE)
            workDirectory ?: missingMandatoryProperties.add(WORK_DIRECTORY)
        }

        return missingMandatoryProperties
    }

    private fun getAppPrototype(configuration: Configuration, service: ConfService): AppPrototype? {
        val cfgAppPrototype = appPrototype?.let { service.retrieveObject(it) }

        return if (cfgAppPrototype != null) AppPrototype(cfgAppPrototype)
        else appPrototype?.let { configuration.asMapByReference.get(appPrototype) } as AppPrototype?
    }

    // FIXME ignoring unchangeable `flexibleProperties` when application is backupServer (`isPrimary` is CFGFalse)
    override fun checkUnchangeableProperties(cfgObject: CfgObject) = checkUnchangeableProperties(this, cfgObject)

    private fun isServer(type: String) = !notServerAppTypes.contains(type)

    override fun getReferences(): Set<ConfigurationObjectReference<*>> =
        referenceSetBuilder()
            .add(appPrototype)
            .add(appServers?.mapNotNull { it.appServer })
            .add(folder)
            .add(serverInfo?.backupServer)
            .add(serverInfo?.host)
            .add(tenants)
            .toSet()
}

data class ConnInfo(
    val appParams: String? = null,
    val appServer: ApplicationReference? = null,
    val charField1: String? = null,
    val charField2: String? = null,
    val charField3: String? = null,
    val charField4: String? = null,
    val connProtocol: String? = null,
    val description: String? = null,
    val id: String? = null,
    val longField1: Int? = null,
    val longField2: Int? = null,
    val longField3: Int? = null,
    val longField4: Int? = null,
    val mode: String? = null,
    val proxyParams: String? = null,
    val timeoutLocal: Int? = null,
    val timeoutRemote: Int? = null,
    val transportParams: String? = null
) {
    constructor(cfgConnInfo: CfgConnInfo) : this(
        appParams = cfgConnInfo.appParams,
        appServer = if (cfgConnInfo.appServer != null) ApplicationReference(cfgConnInfo.appServer.name) else null,
        charField1 = cfgConnInfo.charField1,
        charField2 = cfgConnInfo.charField2,
        charField3 = cfgConnInfo.charField3,
        charField4 = cfgConnInfo.charField4,
        connProtocol = cfgConnInfo.connProtocol,
        description = cfgConnInfo.description,
        id = cfgConnInfo.id,
        longField1 = cfgConnInfo.longField1,
        longField2 = cfgConnInfo.longField2,
        longField3 = cfgConnInfo.longField3,
        longField4 = cfgConnInfo.longField4,
        mode = cfgConnInfo.mode.toShortName(),
        proxyParams = cfgConnInfo.proxyParams,
        timeoutLocal = cfgConnInfo.timoutLocal,
        timeoutRemote = cfgConnInfo.timoutRemote,
        transportParams = cfgConnInfo.transportParams
    )

    fun toCfgConnInfo(application: CfgApplication): CfgConnInfo {
        val service = application.configurationService
        val connInfo = CfgConnInfo(application.configurationService, application)

        setProperty("appParams", appParams, connInfo)
        setProperty("appServerDBID", service.getObjectDbid(appServer), connInfo)
        setProperty("charField1", charField1, connInfo)
        setProperty("charField2", charField2, connInfo)
        setProperty("charField3", charField3, connInfo)
        setProperty("charField4", charField4, connInfo)
        setProperty("connProtocol", connProtocol, connInfo)
        setProperty("description", description, connInfo)
        setProperty("id", id, connInfo)
        setProperty("longField1", longField1, connInfo)
        setProperty("longField2", longField2, connInfo)
        setProperty("longField3", longField3, connInfo)
        setProperty("longField4", longField4, connInfo)
        setProperty("mode", toCfgTraceMode(mode), connInfo)
        setProperty("proxyParams", proxyParams, connInfo)
        setProperty("timoutLocal", timeoutLocal, connInfo)
        setProperty("timoutRemote", timeoutRemote, connInfo)
        setProperty("transportParams", transportParams, connInfo)

        return connInfo
    }
}

data class Server(
    val attempts: Int? = null,
    val backupServer: ApplicationReference? = null,
    val host: HostReference? = null,
    val timeout: Int? = null
) {
    // port is populated from default portInfos by config server. Since it's redundant we left it out of the model.

    constructor(cfgServer: CfgServer) : this(
        attempts = cfgServer.attempts,
        backupServer = if (cfgServer.backupServer != null) ApplicationReference(cfgServer.backupServer.name) else null,
        host = if (cfgServer.host != null) HostReference(cfgServer.host.name) else null,
        timeout = cfgServer.timeout
    )

    fun toUpdatedCfgServer(service: IConfService, cfgServer: CfgServer) = cfgServer.also {
        setProperty("attempts", attempts, it)
        setProperty("backupServerDBID", service.getObjectDbid(backupServer), it)
        setProperty("hostDBID", service.getObjectDbid(host), it)
        setProperty("port", (it.parent as CfgApplication).portInfos.find { it.id == "default" }?.port, it)
        setProperty("timeout", timeout, it)
    }
}

data class PortInfo(
    val appParams: String?,
    val charField1: String? = null,
    val charField2: String? = null,
    val charField3: String? = null,
    val charField4: String? = null,
    val connProtocol: String?,
    val description: String?,
    val id: String,
    val longField1: Int? = null,
    val longField2: Int? = null,
    val longField3: Int? = null,
    val longField4: Int? = null,
    val port: String,
    val transportParams: String?
) {
    constructor(cfgPortInfo: CfgPortInfo) : this(
        appParams = cfgPortInfo.appParams,
        charField1 = cfgPortInfo.charField1,
        charField2 = cfgPortInfo.charField2,
        charField3 = cfgPortInfo.charField3,
        charField4 = cfgPortInfo.charField4,
        connProtocol = cfgPortInfo.connProtocol,
        description = cfgPortInfo.description,
        id = cfgPortInfo.id,
        longField1 = cfgPortInfo.longField1,
        longField2 = cfgPortInfo.longField2,
        longField3 = cfgPortInfo.longField3,
        longField4 = cfgPortInfo.longField4,
        port = cfgPortInfo.port,
        transportParams = cfgPortInfo.transportParams
    )

    fun toCfgPortInfo(application: CfgApplication): CfgPortInfo {
        val portInfo = CfgPortInfo(application.configurationService, application)

        setProperty("appParams", appParams, portInfo)
        setProperty("charField1", charField1, portInfo)
        setProperty("charField2", charField2, portInfo)
        setProperty("charField3", charField3, portInfo)
        setProperty("charField4", charField4, portInfo)
        setProperty("connProtocol", connProtocol, portInfo)
        setProperty("description", description, portInfo)
        setProperty("id", id, portInfo)
        setProperty("longField1", longField1, portInfo)
        setProperty("longField2", longField2, portInfo)
        setProperty("longField3", longField3, portInfo)
        setProperty("longField4", longField4, portInfo)
        setProperty("port", port, portInfo)
        setProperty("transportParams", transportParams, portInfo)

        return portInfo
    }
}

private val notServerAppTypes = setOf(
    "advisors",
    "agentdesktop",
    "agentview",
    "automatedworkflowengine",
    "billingclient",
    "ccview",
    "cmclient",
    "contactservermanager",
    "contentanalyzer",
    "eaclient",
    "gcnclient",
    "genesysadministrator",
    "genericclient",
    "infomartstatconfigurator",
    "interactionroutingdesigner",
    "interactionworkspace",
    "itcutility",
    "iwdmanager",
    "knowledgemanager",
    "lme",
    "reservedguiapplication1",
    "reservedguiapplication2",
    "responsemanager",
    "sce",
    "sci",
    "strategybuilder",
    "strategyscheduler",
    "strategysimulator",
    "thirdpartyapp",
    "virtualinteractivet",
    "voipdevice",
    "vssconsole",
    "webclient",
    "wfmclient",
    "wfmwebservices"
)
