package com.nuecho.genesys.cli.services

interface Service {
    fun open()
    fun close()
}

fun <T> withService(service: Service, function: (service: Service) -> T): T {
    service.open()
    try {
        return function(service)
    } finally {
        service.close()
    }
}
