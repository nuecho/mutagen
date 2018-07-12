package com.nuecho.genesys.cli.services

import com.nuecho.genesys.cli.preferences.environment.Environment
import io.mockk.spyk

object ServiceMocks {
    fun mockConfService() = spyk(ConfService(Environment(host = "test", user = "test", rawPassword = "test"), true))
}
