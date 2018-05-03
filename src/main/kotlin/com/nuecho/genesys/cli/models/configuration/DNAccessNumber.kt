package com.nuecho.genesys.cli.models.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.genesyslab.platform.applicationblocks.com.objects.CfgDNAccessNumber
import com.nuecho.genesys.cli.getPrimaryKey

data class DNAccessNumber(
    val number: String,
    val switch: String?

) {
    val primaryKey: String
        @JsonIgnore
        get() = number

    constructor(dnAccessNumber: CfgDNAccessNumber) : this(
        number = dnAccessNumber.number,
        switch = dnAccessNumber.switch.getPrimaryKey()
    )
}
