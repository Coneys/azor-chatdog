@file:OptIn(ExperimentalUuidApi::class)

package com.github.coneys.kazor.llm.koog

import com.github.coneys.kazor.chat.Response
import com.github.coneys.kazor.session.LlmChatSession
import com.github.coneys.kazor.session.SessionHistory
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import com.github.coneys.kazor.llm.LlmClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KoogChatSession(
    private val executor: PromptExecutor,
    private val model: LLModel,
    private var history: SessionHistory,
    private var systemPrompt: String,
    private val options: LlmClient.Options
) : LlmChatSession {

    override fun sendMessage(text: String): Response {
        // Update history with the user's message first
        val currentHistory = history.withUserMessage(text)

        // Execute a single, non-streamed KOOG agent run
        val output: String = try {
            runBlocking {
                executor.execute(currentHistory.asKoogPrompt(), model).first().content
            }
        } catch (e: Throwable) {
            "[KOOG error] ${e.message ?: e.toString()}"
        }

        // Append assistant response to history
        history = currentHistory.withModelResponse(output)

        return Response(text = output)
    }

    private fun SessionHistory.asKoogPrompt(): Prompt {
        return prompt(Uuid.random().toString(),options.asLLMParams()) {
            entries.forEach {
                system(systemPrompt)
                when (it.role) {
                    "user" -> user(it.messageText)
                    "assistant" -> assistant(it.messageText)
                }
            }
        }
    }

    private fun LlmClient.Options.asLLMParams(): LLMParams {
        val map = buildMap {
            topK?.let { put("top_k", JsonPrimitive(it)) }
            topP?.let { put("top_p", JsonPrimitive(it)) }
        }
        return LLMParams(temperature = temperature?.toDouble(), additionalProperties = map)
    }

    override fun getHistory(): SessionHistory = history
}
