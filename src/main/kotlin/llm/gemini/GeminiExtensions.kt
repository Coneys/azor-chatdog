package llm.gemini

import com.google.genai.types.Content
import com.google.genai.types.Part
import message.Message
import session.SessionHistory

internal fun SessionHistory.asGeminiContent(): List<Content> {
    return entries.map {
        Content.builder()
            .role(it.role)
            .parts(it.messages.map(::asPart))
            .build()
    }
}

private fun asPart(message: Message): Part {
    return when (message) {
        is Message.Text -> Part.fromText(message.text)
    }
}