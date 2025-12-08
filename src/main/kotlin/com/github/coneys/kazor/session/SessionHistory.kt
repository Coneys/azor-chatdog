package com.github.coneys.kazor.session

import kotlinx.serialization.Serializable
import com.github.coneys.kazor.message.Message

@Serializable
class SessionHistory(val entries: List<Entry>) {
    @Serializable
    class Entry(val role: String, val messages: List<Message>)

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

    fun display(assistantName: String) {
        println("\n--- Wątek sesji ---")
        entries.forEach { displayEntry(it, assistantName, false) }
        println("----------------------------")
    }

    private fun displayEntry(entry: Entry, assistantName: String, usePreview: Boolean) {
        val displayRole = when (entry.role) {
            "user" -> "TY"
            "model" -> assistantName
            else -> entry.role
        }

        val text = entry.messages.firstOrNull()?.let {
            when (it) {
                is Message.Text -> it.text
            }
        } ?: ""


        val preview = if (text.length > 80 && usePreview) text.take(80) + "..." else text

        when (entry.role) {
            "user" -> println("  $displayRole: $preview")
            "model" -> println("  $displayRole: $preview")
            else -> println("  $displayRole: $preview")
        }
    }

    fun withUserMessage(text: String) = SessionHistory(entries + Entry("user", listOf(Message.Text(text))))

    fun withModelResponse(text: String) = SessionHistory(entries + Entry("model", listOf(Message.Text(text))))
    fun lastAgentResponse(): String? = entries.lastOrNull { it.role == "model" }
        ?.messages
        ?.filterIsInstance<Message.Text>()
        ?.lastOrNull()?.text

}