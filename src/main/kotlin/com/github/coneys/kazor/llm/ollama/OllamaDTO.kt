package com.github.coneys.kazor.llm.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("top_k") val topK: Int? = null,
)

@Serializable
internal data class OllamaResponse(
    val response: String? = null,
    val done: Boolean = false
)
