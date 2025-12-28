package com.github.coneys.kazor.session

import ai.koog.prompt.params.LLMParams
import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.llm.LlmClient
import kotlinx.serialization.Serializable
import com.github.coneys.kazor.message.Message
import kotlinx.serialization.json.JsonPrimitive

@Serializable
class SessionHistory(val entries: List<Entry>) {

    @Serializable
    class Entry(val role: String, val messages: List<Message>) {
        val messageText get() = messages.filterIsInstance<Message.Text>().joinToString { it.text }
    }

    val isEmpty get() = entries.isEmpty()
    val size get() = entries.size

    companion object {
        val empty = SessionHistory(emptyList())
    }

    fun withLastDropped(elements: Int) = SessionHistory(entries.dropLast(elements))

    fun displaySummary(assistantName: String) {
        val total = entries.size
        if (total == 0) return

        if (total > 2) {
            println("\n--- Wątek sesji wznowiony ---")
            val omitted = total - 2
            println("(Pominięto $omitted wcześniejszych wiadomości)")
        } else {
            println("\n--- Wątek sesji ---")
        }

        val lastTwo = entries.takeLast(2)

        for (entry in lastTwo) {
            displayEntry(entry, assistantName, true)
        }

        println("----------------------------")
    }

    fun display(currentAssistantName: String, assistantSwitches: List<SessionAssistantSwitch>) {
        println("\n--- Wątek sesji ---")

        // Determine the initial assistant in effect for the very first entry
        // If there are recorded switches, the initial assistant is the one used BEFORE the first switch
        // Otherwise, use the currently selected assistant name
        val sortedSwitches = assistantSwitches.sortedBy { it.atEntryIndex }
        var effectiveAssistantName = if (sortedSwitches.isNotEmpty()) {
            sortedSwitches.first().switchFrom.rawValue
        } else {
            currentAssistantName
        }

        // Iterate once through entries and switches advancing the switch pointer when its index passes
        var switchPointer = 0
        entries.forEachIndexed { index, entry ->
            while (switchPointer < sortedSwitches.size && sortedSwitches[switchPointer].atEntryIndex <= index) {
                // Switch becomes effective starting at its index
                effectiveAssistantName = sortedSwitches[switchPointer].switchTo.rawValue
                switchPointer++
            }

            displayEntry(entry, effectiveAssistantName, false)
        }
        println("----------------------------")
    }

    private fun displayEntry(entry: Entry, assistantName: String, usePreview: Boolean) {
        val displayRole = when (entry.role) {
            "user" -> "TY"
            "model" -> assistantName
            else -> entry.role.uppercase()
        }

        val text = entry.messages.firstOrNull()?.let {
            when (it) {
                is Message.Text -> it.text
            }
        } ?: ""

        val preview = if (text.length > 80 && usePreview) text.take(80) + "..." else text

        println("  $displayRole: $preview")
    }

    fun withUserMessage(text: String) = SessionHistory(entries + Entry("user", listOf(Message.Text(text))))
    fun withAssistantSwitch(assistant: Assistant) =
        SessionHistory(
            entries + Entry(
                "user",
                listOf(Message.Text("[ASSISTANT SWITCH] Active assistant changed to ${assistant.name}. From now on, use the new system prompt"))
            )
        )

    fun withModelResponse(text: String) = SessionHistory(entries + Entry("model", listOf(Message.Text(text))))

    fun lastAgentResponse(): String? = entries.lastOrNull { it.role == "model" }
        ?.messages
        ?.filterIsInstance<Message.Text>()
        ?.lastOrNull()?.text

    fun asText(): String = entries.joinToString("\n") { it.messageText }
}
