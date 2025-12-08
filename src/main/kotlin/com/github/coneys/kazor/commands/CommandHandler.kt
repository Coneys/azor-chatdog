package com.github.coneys.kazor.commands

import com.github.coneys.kazor.session.SessionManager
import com.github.coneys.kazor.assistant.Assistant
import com.github.coneys.kazor.console.Console
import com.github.coneys.kazor.session.SessionSnapshot

class CommandHandler(
    private val exportSessionToPdf: (SessionSnapshot) -> Unit = {},
    private val removeSessionCommand: (SessionManager) -> Unit = {},
    private val recordAudioFromLastResponse: (SessionSnapshot) -> Unit,
) {

    private val VALID_SLASH_COMMANDS = setOf("/exit", "/quit", "/switch", "/help", "/session", "/audio")

    /**
     * Handles slash commands.
     * @return true jeśli należy zakończyć program
     */
    fun handleCommand(input: String): Boolean {
        val parts = input.trim().split(" ")
        val command = parts[0].lowercase()

        // Nieznana komenda
        if (command !in VALID_SLASH_COMMANDS) {
            Console.printError("Błąd: Nieznana komenda: $command. Użyj /help.")
            val current = SessionManager.currentSession
            Console.displayHelp(current.sessionId)
            return false
        }
        if (command == "/audio") {
            Console.printInfo("Zapisywanie ostatniej odpowiedzi jako audio...")
            recordAudioFromLastResponse(SessionManager.currentSession.asSnapshot())
            Console.printInfo("Zapisywanie zakończone")
        }

        // --- HELP ---
        if (command == "/help") {
            val current = SessionManager.currentSession
            Console.displayHelp(current.sessionId)
        }

        // --- EXIT ---
        if (command == "/exit" || command == "/quit") {
            Console.printInfo("\nZakończenie czatu. Uruchamianie procedury finalnego zapisu...")
            return true
        }

        // --- SWITCH ---
        if (command == "/switch") {
            if (parts.size == 2) {
                val newId = parts[1]
                val current = SessionManager.currentSession

                if (newId == current.sessionId) {
                    Console.printInfo("Jesteś już w tej sesji.")
                } else {
                    SessionManager.switchToSession(newId)
                }
            } else {
                Console.printError("Błąd: Użycie: /switch <SESSION-ID>")
            }

            return false
        }

        // --- SESSION ---
        if (command == "/com/github/coneys/kazor/session") {
            if (parts.size < 2) {
                Console.printError("Błąd: Komenda /session wymaga podkomendy (list, display, pop, clear, new).")
                return false
            }

            handleSessionSubcommand(parts[1].lowercase())
            return false
        }

        // --- PDF ---
        if (command == "/pdf") {
            val current = SessionManager.currentSession
            exportSessionToPdf(current.asSnapshot())
            return false
        }

        return false
    }

    private fun handleSessionSubcommand(sub: String) {
        val current = SessionManager.currentSession

        when (sub) {
            "list" -> SessionManager.listAvailableSessions()

            "display" -> {
                current.asSnapshot().history.display(Assistant.createAzorAssistant().name)
            }

            "pop" -> {
                val success = current.popLastExchange()
                if (success) {
                    Console.printInfo("Usunięto ostatnią parę wpisów (TY i ${current.assistantName}).")
                    current.getHistory().displaySummary(current.assistantName)
                } else {
                    Console.printError("Błąd: Historia jest pusta lub niekompletna (wymaga co najmniej jednej pary).")
                }
            }

            "clear" -> {
                current.clearHistory()
                Console.printInfo("Historia bieżącej sesji została wyczyszczona. Ilość wpisów ${current.getHistory().size}")
            }

            "new" -> {
                SessionManager.initializeNewSession()
            }

            "remove" -> removeSessionCommand(SessionManager)

            else -> Console.printError("Błąd: Nieznana podkomenda dla /session: $sub. Użyj /help.")
        }
    }
}
