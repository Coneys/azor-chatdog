package llm.gemini

import session.LlmChatSession
import chat.Response
import com.google.genai.Chat
import message.Message
import session.SessionHistory
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