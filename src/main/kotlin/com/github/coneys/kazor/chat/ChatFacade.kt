package com.github.coneys.kazor.chat

import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.llm.ollama.OllamaLLMClient
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.session.SessionSnapshot
import com.github.coneys.kazor.session.SessionHistory
import com.github.coneys.kazor.session.SessionStorage
import com.github.coneys.kazor.session.name.SessionName
import your.pkg.gemini.GeminiLLMClient
import java.util.UUID

class ChatFacade(
    val assistant: Assistant,
    val sessionId: String = UUID.randomUUID().toString(),
    initialHistory: SessionHistory = SessionHistory.Companion.empty,
    initialName: String? = null
) {
    private val llmClient: LlmClient
    private var llmChatSession: LlmChatSession
    private val maxContextTokens = 32768
    var sessionName: String? = initialName

    init {
        val engine = (System.getenv("ENGINE") ?: "GEMINI").uppercase()
        llmClient = when (engine) {
            "GEMINI" -> GeminiLLMClient.fromEnvironment()
            "OLLAMA" -> OllamaLLMClient()
            else -> error("Unkown engine $engine")
        }

        llmChatSession = initializeSession(initialHistory)

        initializeName(initialHistory.entries.firstOrNull()?.messageText)
    }

    private fun initializeName(firstMessage: String?) {
        if (sessionName != null) return
        if (firstMessage == null) return
        println("Initializing session name...")
        sessionName = SessionName(llmClient).generateFor(firstMessage)
    }

    companion object Companion {
        fun loadFromFile(sessionId: String): ChatFacade? {
            val session = SessionStorage.loadSession(sessionId) ?: return null
            val assistant = Assistant.getByName(session.assistantName)

            return ChatFacade(assistant, sessionId, session.history, session.name)
        }
    }

    internal fun asSnapshot() =
        SessionSnapshot(
            llmChatSession.getHistory(),
            llmClient!!.getModelName(),
            sessionId,
            assistant.systemPrompt,
            sessionName
        )

    fun saveToFile(): Boolean {
        return SessionStorage.saveSession(asSnapshot())
    }

    fun sendMessage(text: String): Response {
        val response = llmChatSession.sendMessage(text)

        initializeName(text)

        val totalTokens = countTokens()

        appendToWal(
            sessionId = sessionId,
            prompt = text,
            responseText = response.text,
            totalTokens = totalTokens,
            modelName = llmClient.getModelName()
        )

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
        get() = assistant.name.rawValue
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

