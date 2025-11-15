package llm

import session.LlmChatSession
import session.SessionHistory

interface LlmClient {
    fun createChatSession(systemInstruction: String, history: SessionHistory, options: Options): LlmChatSession
    fun getModelName(): String
    fun countHistoryTokens(history: SessionHistory): Int
    fun readyForUseMessage(): String

    class Options(
        val temperature: Float? = null,
        val topP: Float? = null,
        val topK: Int? = null,
        val thinkingBudget: Int = 0
    ) {
        companion object {
            val empty = Options()
        }
    }
}