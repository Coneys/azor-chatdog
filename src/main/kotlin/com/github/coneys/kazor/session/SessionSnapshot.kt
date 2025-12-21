package com.github.coneys.kazor.session

import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.assistant.AssistantName
import kotlinx.serialization.Serializable

@Serializable
class SessionSnapshot(
    val history: SessionHistory,
    val model: String,
    val sessionId: String,
    val systemRole: String,
    val name: String? = null,
    val assistantName: AssistantName? = null
) {
    val isEmpty: Boolean get() = history.isEmpty && name == null
    val presentationName: String get() = name ?: sessionId
}