package com.github.coneys.kazor.assistant

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class AssistantName(val rawValue: String){
    override fun toString() = rawValue
}