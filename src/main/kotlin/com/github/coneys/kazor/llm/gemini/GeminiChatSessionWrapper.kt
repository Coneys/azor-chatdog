package com.github.coneys.kazor.llm.gemini

import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.chat.Response
import com.google.genai.Chat
import com.github.coneys.kazor.message.Message
import com.github.coneys.kazor.session.SessionHistory
import kotlin.jvm.optionals.getOrNull

class GeminiChatSessionWrapper(
    private val chat: Chat
) : LlmChatSession {

    override fun sendMessage(text: String): Response {
        return Response(chat.sendMessage(text).text()!!)
    }

    override fun getHistory(): SessionHistory {
        val entries = chat.getHistory(false).map {
            val messages = it.parts().getOrNull().orEmpty().mapNotNull { part ->
                val partText = part.text().getOrNull()
                partText ?: return@mapNotNull null
                Message.Text(partText)
            }
            SessionHistory.Entry(it.role().get(), messages)
        }

        return SessionHistory(entries)
    }

}