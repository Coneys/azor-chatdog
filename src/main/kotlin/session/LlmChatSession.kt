package session

import chat.Response

interface LlmChatSession {
    fun sendMessage(text: String): Response
    fun getHistory(): SessionHistory
}