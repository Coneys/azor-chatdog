package com.github.coneys.kazor.llm.gemini

import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.genai.types.ThinkingConfig
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.message.Message
import com.github.coneys.kazor.session.SessionHistory

internal fun GenerateContentConfig.Builder.applyOptions(options: LlmClient.Options) = this.apply {

    options.temperature?.let { temperature(it) }
    options.topP?.let { topP(it) }
    options.topK?.let { topK(it.toFloat()) }

    val builder = ThinkingConfig.builder().thinkingBudget(options.thinkingBudget)
    thinkingConfig(builder.build())
}

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