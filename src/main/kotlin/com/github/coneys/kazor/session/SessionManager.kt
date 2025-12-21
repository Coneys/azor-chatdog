package com.github.coneys.kazor.session

import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.chat.ChatFacade
import com.github.coneys.kazor.console.Console
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SessionManager {
    lateinit var currentSession: ChatFacade

    fun initializeFromCli(cliSessionId: String?): ChatFacade {
        val session: ChatFacade

        if (cliSessionId != null) {
            val loadedSession = ChatFacade.loadFromFile(cliSessionId)

            session = if (loadedSession == null) {
                val fresh = ChatFacade(Assistant.default)
                println("Rozpoczęto nową sesję z ID: ${fresh.sessionId}")
                fresh
            } else {
                loadedSession
            }
            Console.displayHelp(session.sessionId)

            if (!session.isEmpty()) {
                session.getHistory().displaySummary(session.assistantName)
            }

        } else {
            session = createNewSession(Assistant.default)
        }

        currentSession = session
        return session
    }

    fun initializeNewSession() {
        SessionStorage.saveSession(currentSession.asSnapshot())
        currentSession = createNewSession(Assistant.default)
    }

    private fun createNewSession(assistant: Assistant): ChatFacade {
        println("Rozpoczynanie nowej sesji.")
        val session = ChatFacade(assistant)
        Console.displayHelp(session.sessionId)
        return session
    }

    fun cleanupAndSave() {
        if (currentSession.isEmpty()) {
            Console.printInfo("\nSesja jest pusta/niekompletna. Pominięto finalny zapis.")
            return
        }

        Console.printInfo("\nFinalny zapis historii sesji: ${currentSession.sessionId}")
        currentSession.saveToFile()

        Console.displayFinalInstructions(currentSession.sessionId)
    }

    fun switchToSession(newId: String) {
        SessionStorage.saveSession(currentSession.asSnapshot())

        val newSession = ChatFacade.Companion.loadFromFile(newId)

        if (newSession != null) {
            currentSession = newSession
            Console.printInfo("--- Przełączono na sesję: ${newSession.sessionId} ---")
            newSession.getHistory().displaySummary(newSession.assistantName)
        } else {
            Console.printError("Nie można wczytać sesji o ID: $newId")
        }

    }

    fun listAvailableSessions() {
        val sessions = SessionStorage.listSessions()

        if (sessions.isEmpty()) {
            Console.printHelp("\nBrak zapisanych sesji.")
            return
        }

        Console.printHelp("\n--- Dostępne zapisane sesje (ID) ---")

        sessions.forEach {
            val snapshot = it.first
            val messagesCount = snapshot.history.size
            val lastActivity = it.second
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

            val consoleMessage = buildString {
                append("- ID: ${snapshot.sessionId} (")
                snapshot.name?.let { name -> append(" \"$name\" ") }
                append("Wiadomości: $messagesCount, Ost. aktywność: $lastActivity)")
            }
            Console.printHelp(consoleMessage)

        }

        Console.printHelp("------------------------------------")
    }

}