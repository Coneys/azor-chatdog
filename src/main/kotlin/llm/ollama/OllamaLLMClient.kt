package llm.ollama

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import llm.LlmClient
import message.Message
import session.LlmChatSession
import session.SessionHistory

private val ollamaHttp = HttpClient {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        level = LogLevel.NONE
    }
}

class OllamaLLMClient(
    private val modelName: String = "mistral"
) : LlmClient {

    override fun createChatSession(
        systemInstruction: String,
        history: SessionHistory,
        options: LlmClient.Options
    ): LlmChatSession {
        return OllamaChatSession(
            model = modelName,
            systemInstruction = systemInstruction,
            http = ollamaHttp,
            history = history,
            options = LlmClient.Options(temperature = 0f, topK = 1)
        )
    }

    override fun getModelName(): String = modelName

    override fun readyForUseMessage(): String {
        return "🟢 Połączono z lokalnym modelem Ollama: $modelName"
    }

    override fun countHistoryTokens(history: SessionHistory): Int {
        // Ollama nie ma natywnego liczenia tokenów — proste przybliżenie
        return history.entries.flatMap { it.messages.filterIsInstance<Message.Text>() }
            .sumOf { it.text.split(Regex("\\s+")).size }
    }
}
