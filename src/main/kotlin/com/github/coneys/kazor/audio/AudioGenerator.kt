package com.github.coneys.kazor.audio

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.Duration.Companion.minutes

class AudioGenerator(
    private val client: HttpClient,
    private val host: String = "0.0.0.0",
    private val port: String = "8000"
) {

    enum class Model {
        First, Second
    }

    suspend fun from(prompt: String, model: Model = Model.First): ByteArray? {
        val formParameters = Parameters.build {
            append("text", prompt)
            append("model_id", model.ordinal.toString())
        }
        return client.submitForm("http://$host:$port/synthesize", formParameters)
            .takeIf { it.status.isSuccess() }
            ?.bodyAsBytes()
    }

    companion object {
        val default = AudioGenerator(HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 10.minutes.inWholeMilliseconds
            }
        })
    }
}