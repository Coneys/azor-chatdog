package com.github.coneys.kazor.session

import kotlinx.serialization.Serializable

@Serializable
class SessionSnapshot(
    val history: SessionHistory,
    val model: String,
    val sessionId: String,
    val systemRole: String,
    val name: String? = null
) {
    val isEmpty: Boolean get() = history.isEmpty && name == null
    val presentationName: String get() = name ?: sessionId
}