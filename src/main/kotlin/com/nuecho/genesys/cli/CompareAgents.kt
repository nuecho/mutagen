package com.nuecho.genesys.cli

import com.genesyslab.platform.applicationblocks.com.IConfService
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery
import com.genesyslab.platform.configuration.protocol.types.CfgFlag
import difflib.DiffUtils
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "agents", description = ["Compare Genesys agent configuration between two instances."])
class CompareAgents : BasicCommand(), Callable<Void> {
    @CommandLine.ParentCommand
    private val compare: Compare? = null

    @Throws(Exception::class)
    override fun call(): Void? {
        var service: IConfService? = null
        try {
            service = compare!!.connect()

            val query = CfgPersonQuery()
            query.isAgent = CfgFlag.CFGTrue.ordinal()
            val persons = service.retrieveMultipleObjects(CfgPerson::class.java, query)
            val left = persons.map { person -> person.userName }
            val right = left.toMutableList()

            // Sample test, to use with 192.168.129.85 server
            right.removeAt(10)
            right.removeAt(10)
            right.removeAt(10)

            right.removeAt(50)
            right.removeAt(50)
            right.removeAt(50)

            right[5] = "testerpatate"

            val diff = DiffUtils.diff(left, right)
            println(diff.deltas)
        } finally {
            GenesysServices.releaseConfigurationService(service)
        }

        return null
    }
}
