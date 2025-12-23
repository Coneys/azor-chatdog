package com.github.coneys.kazor.llm.ollama

import com.github.coneys.kazor.chat.Response
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.message.Message
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.session.SessionHistory

class OllamaChatSession(
    private val model: String,
    private val systemInstruction: String,
    private val http: HttpClient,
    private var history: SessionHistory,
    private val options: LlmClient.Options
) : LlmChatSession {

    override fun getHistory(): SessionHistory = history

    override fun sendMessage(text: String): Response {
        val fullPrompt = buildPrompt(text)

        val request = OllamaRequest(
            model = model,
            prompt = fullPrompt,
            stream = false,

            temperature = options.temperature?.toDouble(),
            topP = options.topP?.toDouble(),
            topK = options.topK,
        )

        val result = runBlocking {
            http.post("http://localhost:11434/api/generate") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(request)
            }.body<OllamaResponse>()
        }

        val answer = result.response ?: "(brak odpowiedzi)"

        // Zapis do historii JAK W GEMINI WRAPPER
        history = history.withUserMessage(text).withModelResponse(answer)

        return Response(text = answer)
    }

    /** Buduje prompt w formacie instrukcji + historia + nowe pytanie. */
    private fun buildPrompt(userMsg: String): String {
        val sb = StringBuilder()

        sb.appendLine("### System:")
        sb.appendLine(systemInstruction)
        sb.appendLine()

        for (entry in history.entries) {
            when (entry.role) {
                "user" -> {
                    sb.appendLine("### User:")
                    entry.messages.forEach {
                        when (it) {
                            is Message.Text -> sb.appendLine(it.text)
                        }
                    }
                    sb.appendLine()
                }

                "assistant" -> {
                    sb.appendLine("### Assistant:")
                    entry.messages.forEach {
                        when (it) {
                            is Message.Text -> sb.appendLine(it.text)
                        }
                    }
                    sb.appendLine()
                }
            }
        }

        sb.appendLine("### User:")
        sb.appendLine(userMsg)

        return sb.toString()
    }
}
