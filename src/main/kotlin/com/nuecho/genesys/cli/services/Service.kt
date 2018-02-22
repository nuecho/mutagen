package com.nuecho.genesys.cli.services

interface Service {
    fun open()
    fun close()
}

fun withService(service: Service, function: (service: Service) -> Any?) {
    service.open()
    try {
        function(service)
    } finally {
        service.close()
    }
}
