package your.pkg.gemini

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.tokenizer.SimpleRegexBasedTokenizer
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.llm.koog.KoogChatSession
import com.github.coneys.kazor.session.SessionHistory

class GeminiLLMClient(
    private val modelName: String,
    private val apiKey: String
) : LlmClient {

    init {
        println("🤖 Przygotowywanie klienta Gemini...")
        require(apiKey.isNotBlank()) { "API key cannot be empty" }
    }

    private val llmModel: LLModel = GoogleModels.Gemini2_5Flash
    private val executor = SingleLLMPromptExecutor(GoogleLLMClient(System.getenv("GEMINI_API_KEY")))


    init {
        println(readyForUseMessage())

    }

    companion object {
        fun fromEnvironment(): GeminiLLMClient {
            val model = System.getenv("MODEL_NAME") ?: "gemini-2.5-flash"
            val key = System.getenv("GEMINI_API_KEY") ?: ""
            return GeminiLLMClient(model, key)
        }
    }

    override fun createChatSession(
        systemInstruction: String,
        history: SessionHistory,
        options: LlmClient.Options
    ): LlmChatSession {
        return KoogChatSession(executor, llmModel, history, systemInstruction, options)
    }

    override fun getModelName() = llmModel.id
    override fun countHistoryTokens(history: SessionHistory): Int {
        return try {
            SimpleRegexBasedTokenizer().countTokens(history.asText())
        } catch (e: Exception) {
            println("⚠️ Błąd podczas liczenia tokenów: $e")
            0
        }
    }

    override fun readyForUseMessage(): String {
        val masked = if (apiKey.length <= 8) "****"
        else "${apiKey.take(4)}...${apiKey.takeLast(4)}"
        return "✅ Klient Gemini gotowy do użycia (Model: $modelName, Key: $masked)"
    }
}
