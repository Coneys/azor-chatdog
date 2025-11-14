package session

import console.Console
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import java.time.Instant

object SessionStorage {
    val logDir = Paths.get(System.getProperty("user.home"), ".kazor").toFile().also { it.mkdirs() }

    private val String.file
        get() = File(logDir, "${this}-log.json").also {
            it.createNewFile()
        }

    fun listSessions(): List<Pair<SessionSnapshot, Instant>> {
        return logDir.listFiles().orEmpty().toList().filter { it.name.endsWith("-log.json") }
            .map {
                val snapshot = Json.decodeFromString<SessionSnapshot>(it.readText())
                snapshot to Instant.ofEpochMilli(it.lastModified())
            }
            .sortedByDescending { it.second }
    }

    fun loadSession(sessionId: String): SessionSnapshot? {
        val file = sessionId.file
        return runCatching {
            Json.Default.decodeFromString<SessionSnapshot>(file.readText())
        }
            .onFailure { println("Failed to load session history from file: ${file.absolutePath}") }
            .getOrNull()
    }

    fun saveSession(sessionSnapshot: SessionSnapshot): Boolean {
        Console.printInfo("Zapisuję bieżącą sesję: ${SessionManager.currentSession.sessionId}...")

        val text = Json.Default.encodeToString(sessionSnapshot)
        return runCatching {
            sessionSnapshot.sessionId.file.writeText(text)
            true
        }.getOrDefault(false)
    }
}