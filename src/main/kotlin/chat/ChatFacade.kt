package chat

import assistant.Assistant
import llm.LlmClient
import llm.ollama.OllamaLLMClient
import session.LlmChatSession
import session.SessionSnapshot
import session.SessionHistory
import session.SessionStorage
import your.pkg.gemini.GeminiLLMClient
import java.util.UUID

class ChatFacade(
    val assistant: Assistant,
    val sessionId: String = UUID.randomUUID().toString(),
    initialHistory: SessionHistory = SessionHistory.Companion.empty
) {
    private var llmClient: LlmClient? = null
    private var llmChatSession: LlmChatSession
    private val maxContextTokens = 32768

    init {
        val engine = (System.getenv("ENGINE") ?: "OLLAMA").uppercase()
        llmClient = when (engine) {
            "GEMINI" -> GeminiLLMClient.fromEnvironment()
            "OLLAMA" -> OllamaLLMClient()
            else -> error("Unkown engine $engine")
        }

        llmChatSession = initializeSession(initialHistory)
    }


    companion object Companion {
        fun loadFromFile(assistant: Assistant, sessionId: String): ChatFacade? {
            val session = SessionStorage.loadSession(sessionId) ?: return null

            return ChatFacade(assistant, sessionId, session.history)
        }
    }

    internal fun asSnapshot() = SessionSnapshot(llmChatSession.getHistory(), llmClient!!.getModelName(), sessionId, assistant.systemPrompt)

    fun saveToFile(): Boolean {
        return SessionStorage.saveSession(asSnapshot())
    }

    fun sendMessage(text: String): Response {
        val response = llmChatSession.sendMessage(text)

        val totalTokens = countTokens()
        val (success, error) = appendToWal(
            sessionId = sessionId,
            prompt = text,
            responseText = response.text,
            totalTokens = totalTokens,
            modelName = llmClient!!.getModelName()
        )

        // ignore WAL error
        return response
    }

    fun getHistory(): SessionHistory {
        return llmChatSession.getHistory()
    }

    fun clearHistory() {
        llmChatSession = initializeSession(SessionHistory.empty)
        saveToFile()
    }

    private fun initializeSession(initialHistory: SessionHistory): LlmChatSession = llmClient!!.createChatSession(
        systemInstruction = assistant.systemPrompt,
        history = initialHistory,
        options = LlmClient.Options.empty
    )

    fun popLastExchange(): Boolean {
        if (getHistory().size < 2) return false
        val history = getHistory().withLastDropped(2)
        initializeSession(history)
        saveToFile()
        return true
    }

    fun countTokens(): Int = llmClient?.countHistoryTokens(getHistory()) ?: 0

    fun isEmpty(): Boolean = getHistory().size < 2

    fun getRemainingTokens(): Int = maxContextTokens - countTokens()

    fun getTokenInfo(): Triple<Int, Int, Int> {
        val total = countTokens()
        return Triple(total, maxContextTokens - total, maxContextTokens)
    }

    val assistantName: String
        get() = assistant.name
}

data class Response(val text: String)

// Placeholder WAL / sessionFiles
fun appendToWal(
    sessionId: String,
    prompt: String,
    responseText: String,
    totalTokens: Int,
    modelName: String
): Pair<Boolean, String?> = true to null

