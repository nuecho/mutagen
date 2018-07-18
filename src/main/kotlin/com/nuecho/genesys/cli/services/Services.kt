package com.nuecho.genesys.cli.services

import com.nuecho.genesys.cli.core.MetricNames.SERVICE_CLOSE
import com.nuecho.genesys.cli.core.MetricNames.SERVICE_OPEN
import com.nuecho.genesys.cli.core.Metrics.time

object Services {
    fun <T> withService(service: Service, function: (service: Service) -> T): T {
        time(SERVICE_OPEN) { service.open() }

        try {
            return function(service)
        } finally {
            time(SERVICE_CLOSE) { service.close() }
        }
    }
}
