package llm.ollama

import kotlinx.serialization.Serializable

@Serializable
internal data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
internal data class OllamaResponse(
    val response: String? = null,
    val done: Boolean = false
)
