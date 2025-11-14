package session

import kotlinx.serialization.Serializable

@Serializable
class SessionSnapshot(
    val history: SessionHistory,
    val model: String,
    val sessionId: String,
    val systemRole: String
)