package llm

import session.LlmChatSession
import session.SessionHistory

interface LlmClient {
    fun createChatSession(systemInstruction: String, history: SessionHistory, thinkingBudget: Int): LlmChatSession
    fun getModelName(): String
    fun countHistoryTokens(history: SessionHistory): Int
    fun readyForUseMessage(): String
}