package com.github.coneys.kazor.message

import com.github.coneys.kazor.assistant.AssistantName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {

    @Serializable
    @SerialName("text")
    data class Text(val text: String) : Message{
        override fun toString(): String {
            return "Text (${text.take(50)}...)"
        }
    }
}