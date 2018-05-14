package com.nuecho.genesys.cli.commands.audio

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nuecho.genesys.cli.commands.audio.AudioServices.APPLICATION_JSON
import com.nuecho.genesys.cli.commands.audio.AudioServices.CONTENT_TYPE
import com.nuecho.genesys.cli.commands.audio.AudioServices.LOGIN_PATH
import com.nuecho.genesys.cli.commands.audio.AudioServices.LOGIN_SUCCESS_CODE
import com.nuecho.genesys.cli.commands.audio.AudioServices.login
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec
import java.io.ByteArrayOutputStream
import java.net.URL

private const val GAX_URL = "http://genesys.com"

class AudioServicesTest : StringSpec() {
    init {

        "login should properly handle success" {
            val user = "user"
            val password = "password"

            mockHttpClient(LOGIN_SUCCESS_CODE) {
                it.method shouldBe Method.POST
                it.body() shouldBe """{"username":"$user","password":"$password","isPasswordEncrypted":false}"""
                it.headers[CONTENT_TYPE] shouldBe APPLICATION_JSON
                it.path shouldBe "$GAX_URL$LOGIN_PATH"
            }

            login(user, password, false, GAX_URL)
        }

        "login should properly handle error" {
            mockHttpClient(666)
            shouldThrow<AudioServicesException> {
                login("user", "password", false, GAX_URL)
            }
        }
    }

    private fun mockHttpClient(
        statusCode: Int,
        headers: Map<String, List<String>> = emptyMap(),
        validateRequest: (request: Request) -> Unit = {}
    ) {
        val client = object : Client {
            override fun executeRequest(request: Request): Response {
                validateRequest(request)
                return Response(
                    url = URL(GAX_URL),
                    statusCode = statusCode,
                    headers = headers
                )
            }
        }

        FuelManager.instance.client = client
    }

    private fun Request.body() = ByteArrayOutputStream().also {
        bodyCallback?.invoke(this, it, 0)
    }.toString()
}
