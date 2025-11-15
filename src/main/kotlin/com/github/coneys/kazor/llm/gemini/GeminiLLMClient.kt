package your.pkg.gemini

import com.google.genai.Chat
import com.google.genai.Client
import com.google.genai.types.*
import com.github.coneys.kazor.llm.gemini.asGeminiContent
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.llm.gemini.GeminiChatSessionWrapper
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.llm.gemini.applyOptions
import com.github.coneys.kazor.session.SessionHistory
import java.lang.reflect.Field

class GeminiLLMClient(
    private val modelName: String,
    private val apiKey: String
) : LlmClient {

    private val client: Client

    init {
        println("🤖 Przygotowywanie klienta Gemini...")
        require(apiKey.isNotBlank()) { "API key cannot be empty" }

        client = Client.builder()
            .apiKey(apiKey)
            .httpOptions(
                HttpOptions.builder()
                    .apiVersion("v1alpha")
                    .build()
            )
            .build()
        println(readyForUseMessage())

    }

    companion object {
        fun fromEnvironment(): GeminiLLMClient {
            val model = System.getenv("MODEL_NAME") ?: "gemini-2.5-flash"
            val key = System.getenv("GEMINI_API_KEY") ?: ""
            return GeminiLLMClient(model, key)
        }
    }

    /**
     * Wstrzyknięcie historii do ChatBase (comprehensiveHistory + curatedHistory)
     * za pomocą refleksji, bo ChatBase ignoruje argumenty konstruktora.
     */
    private fun injectHistory(chat: Chat, history: List<Content>) {
        val baseClass = chat.javaClass.superclass ?: return

        fun inject(fieldName: String) {
            try {
                val field: Field = baseClass.getDeclaredField(fieldName)
                field.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val list = field.get(chat) as MutableList<Content>

                list.clear()
                list.addAll(history)
            } catch (e: Exception) {
                println("⚠️ Failed to set ChatBase.$fieldName: ${e.message}")
            }
        }

        inject("comprehensiveHistory")
        inject("curatedHistory")
    }

    override fun createChatSession(
        systemInstruction: String,
        history: SessionHistory,
        options: LlmClient.Options
    ): LlmChatSession {
        val builder = GenerateContentConfig.builder()

        val config = builder
            .applyOptions(options)
            .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))

            .build()

        val chat = client.chats.create(modelName, config)

        val converted = history.asGeminiContent()

        injectHistory(chat, converted)

        return GeminiChatSessionWrapper(chat)
    }

    override fun getModelName() = modelName
    override fun countHistoryTokens(history: SessionHistory): Int {
        return try {
            val res = client.models.countTokens(
                modelName,
                history.asGeminiContent(),
                CountTokensConfig.builder().build()
            )
            res.totalTokens().get()
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
