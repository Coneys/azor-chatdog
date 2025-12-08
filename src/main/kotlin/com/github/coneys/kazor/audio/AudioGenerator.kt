package com.github.coneys.kazor.audio

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.Parameters
import io.ktor.http.isSuccess

class AudioGenerator(
    private val client: HttpClient,
    private val host: String = "0.0.0.0",
    private val port: String = "8000"
) {
    suspend fun from(prompt: String): ByteArray? {
        val formParameters = Parameters.build {
            append("text", prompt)
        }
        return client.submitForm("http://$host:$port/synthesize", formParameters)
            .takeIf { it.status.isSuccess() }
            ?.bodyAsBytes()
    }

    companion object {
        val default = AudioGenerator(HttpClient())
    }
}