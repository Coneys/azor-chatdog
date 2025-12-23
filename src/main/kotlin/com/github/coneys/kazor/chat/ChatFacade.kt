package com.github.coneys.kazor.chat

import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.llm.ollama.OllamaLLMClient
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.session.SessionSnapshot
import com.github.coneys.kazor.session.SessionHistory
import com.github.coneys.kazor.session.SessionStorage
import com.github.coneys.kazor.session.SessionAssistantSwitch
import com.github.coneys.kazor.session.name.SessionName
import your.pkg.gemini.GeminiLLMClient
import java.util.UUID

class ChatFacade(
    var assistant: Assistant,
    val sessionId: String = UUID.randomUUID().toString(),
    initialHistory: SessionHistory = SessionHistory.Companion.empty,
    initialName: String? = null,
    initialAssistantSwitches: List<SessionAssistantSwitch> = emptyList(),
) {
    private val llmClient: LlmClient
    private var llmChatSession: LlmChatSession
    private val maxContextTokens = 32768
    var sessionName: String? = initialName
    val assistantSwitches: MutableList<SessionAssistantSwitch> = initialAssistantSwitches.toMutableList()

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

            return ChatFacade(
                assistant = assistant,
                sessionId = sessionId,
                initialHistory = session.history,
                initialName = session.name,
                initialAssistantSwitches = session.assistantSwitches
            )
        }
    }

    internal fun asSnapshot() =
        SessionSnapshot(
            llmChatSession.getHistory(),
            llmClient.getModelName(),
            sessionId,
            sessionName,
            assistant.name,
            assistantSwitches
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

    fun switchAssistant(newAssistant: Assistant) {
        // Current size before adding marker
        val currentSize = llmChatSession.getHistory().entries.size
        // Insert a trace marker into history to indicate persona switch
        val updatedHistory = llmChatSession.getHistory().withAssistantSwitch(newAssistant)
        // Record assistant switch at the index of the inserted marker
        assistantSwitches.add(
            SessionAssistantSwitch(
                atEntryIndex = currentSize,
                switchFrom = this.assistant.name,
                switchTo = newAssistant.name
            )
        )
        // Update assistant and reinitialize session with new system prompt and updated history
        this.assistant = newAssistant
        llmChatSession = initializeSession(updatedHistory)
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

