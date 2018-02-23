package com.nuecho.genesys.cli.commands.agent.status

import com.genesyslab.platform.commons.protocol.Endpoint
import com.genesyslab.platform.commons.protocol.MessageHandler
import com.genesyslab.platform.reporting.protocol.statserver.AgentStatus
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallConsult
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallDialing
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallInbound
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallInternal
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallOnHold
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallOutbound
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallRinging
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.CallUnknown
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.LoggedOut
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.NotReadyForNextCall
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.OffHook
import com.genesyslab.platform.reporting.protocol.statserver.DnActions.WaitForNextCall
import com.genesyslab.platform.reporting.protocol.statserver.DnActionsMask
import com.genesyslab.platform.reporting.protocol.statserver.Notification
import com.genesyslab.platform.reporting.protocol.statserver.NotificationMode
import com.genesyslab.platform.reporting.protocol.statserver.StatisticCategory
import com.genesyslab.platform.reporting.protocol.statserver.StatisticInterval
import com.genesyslab.platform.reporting.protocol.statserver.StatisticMetricEx
import com.genesyslab.platform.reporting.protocol.statserver.StatisticObject
import com.genesyslab.platform.reporting.protocol.statserver.StatisticObjectType
import com.genesyslab.platform.reporting.protocol.statserver.StatisticSubject
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticOpened
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx
import com.nuecho.genesys.cli.GenesysCli
import com.nuecho.genesys.cli.GenesysCliCommand
import com.nuecho.genesys.cli.services.GenesysServices
import com.nuecho.genesys.cli.Logging
import com.nuecho.genesys.cli.Logging.info
import com.nuecho.genesys.cli.commands.agent.Agent
import com.nuecho.genesys.cli.services.StatService
import com.nuecho.genesys.cli.services.StatServiceException
import com.nuecho.genesys.cli.setBits
import com.nuecho.genesys.cli.toConsoleString
import picocli.CommandLine
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// TODO we should review those default values
private const val REFERENCE_ID = 1
private const val DEFAULT_TENANT_NAME = "Resources"
private const val DEFAULT_TENANT_PASSWORD = ""
private const val DEFAULT_STAT_TIME_PROFILE = "Default"

@CommandLine.Command(
    name = "status",
    description = ["Get Agent Status"]
)
class Status : GenesysCliCommand() {
    @CommandLine.ParentCommand
    private var agent: Agent? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-host"],
        description = ["Stat server hostname."]
    )
    private var statHost: String? = null

    @CommandLine.Option(
        arity = "1",
        names = ["--stat-port"],
        description = ["Stat server port."]
    )
    private var statPort: Int? = null

    @CommandLine.Parameters(
        arity = "1",
        index = "0",
        paramLabel = "username",
        description = ["Username of the agent to disconnect."]
    )
    private var username: String? = null

    override fun execute() {
        val statService = StatService(Endpoint(statHost!!, statPort!!))
        val agentStatus = getAgentStatus(username!!, statService)
        println(agentStatus.toConsoleString())
    }

    override fun getGenesysCli(): GenesysCli = agent!!.getGenesysCli()

    fun getAgentStatus(
        username: String,
        statService: StatService,
        timeout: Long = GenesysServices.DEFAULT_CLIENT_TIMEOUT.toLong()
    ): AgentStatus {
        var agentStatus: AgentStatus? = null
        val latch = CountDownLatch(1)

        statService.setMessageHandler(MessageHandler { message ->
            val value = if (message is EventInfo) message.stateValue else return@MessageHandler
            agentStatus = if (value is AgentStatus && value.agentId == username) value else return@MessageHandler

            latch.countDown()
        })

        val referenceId = REFERENCE_ID
        val request = agentStatusRequest(username, referenceId)

        info { "Retrieving agent ($username) status." }

        statService.open()
        try {
            val response = statService.request(request)
            if (response.messageId() != EventStatisticOpened.ID) {
                Logging.debug { "Unexpected response for RequestOpenStatisticEx:\n" + response }
                throw StatServiceException("Error creating statistic. Response: (${response.messageName()}).")
            }

            latch.await(timeout, TimeUnit.SECONDS)

            statService.closeStatistic(referenceId)
        } finally {
            statService.close()
        }

        return agentStatus ?: throw StatServiceException("Failed to get agent status.")
    }

    @Suppress("LongMethod")
    private fun agentStatusRequest(username: String, referenceId: Int): RequestOpenStatisticEx {
        val stat = StatisticObject.create()
        stat.objectId = username
        stat.objectType = StatisticObjectType.Agent
        stat.tenantName = DEFAULT_TENANT_NAME
        stat.tenantPassword = DEFAULT_TENANT_PASSWORD

        val mainMask = DnActionsMask()
        mainMask.setBits(
            WaitForNextCall,
            CallDialing,
            CallRinging,
            OffHook,
            NotReadyForNextCall,
            CallOnHold,
            CallUnknown,
            CallConsult,
            CallInternal,
            CallOutbound,
            CallInbound,
            LoggedOut
        )

        val relMask = DnActionsMask()

        val metric = StatisticMetricEx.create()
        metric.category = StatisticCategory.CurrentState
        metric.mainMask = mainMask
        metric.relativeMask = relMask
        metric.subject = StatisticSubject.DNAction
        metric.timeProfile = DEFAULT_STAT_TIME_PROFILE
        metric.intervalType = StatisticInterval.GrowingWindow

        val notification = Notification.create()
        notification.mode = NotificationMode.Immediate

        val request = RequestOpenStatisticEx.create()
        request.statisticObject = stat
        request.statisticMetricEx = metric
        request.notification = notification

        request.setReferenceId(referenceId)

        return request
    }
}
