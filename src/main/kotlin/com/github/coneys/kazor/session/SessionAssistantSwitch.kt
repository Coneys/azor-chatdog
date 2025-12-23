package com.github.coneys.kazor.session

import com.github.coneys.kazor.assistant.AssistantName
import kotlinx.serialization.Serializable

@Serializable
data class SessionAssistantSwitch(val atEntryIndex: Int, val switchFrom: AssistantName, val switchTo: AssistantName) {
}