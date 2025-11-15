package com.github.coneys.kazor.chat

import com.github.coneys.kazor.session.SessionManager
import com.github.coneys.kazor.commands.CommandHandler
import com.github.coneys.kazor.console.Console
import java.io.InterruptedIOException
import java.nio.channels.ClosedByInterruptException

fun mainLoop(
    commandHandler: CommandHandler,
    getUserInput: () -> String?
) {
    while (true) {
        try {
            val input = getUserInput() ?: continue
            if (input.isBlank()) continue

            // --- Komendy ---
            if (input.startsWith("/")) {
                val shouldExit = commandHandler.handleCommand(input)
                if (shouldExit) break
                continue
            }

            // --- Normalna rozmowa ---
            val session = SessionManager.currentSession

            val response = session.sendMessage(input)

            val (totalTokens, remainingTokens, maxTokens) = session.getTokenInfo()

            Console.printAssistant("\n${session.assistantName}: ${response.text}")
            Console.printInfo("Tokens: $totalTokens (Pozostało: $remainingTokens / $maxTokens)")

            val result = session.saveToFile()
            if (result.not()) {
                Console.printError("Error saving session")
            }

        } catch (ex: InterruptedIOException) {
            Console.printInfo("\nPrzerwano przez użytkownika (Ctrl+C). Uruchamiam finalny zapis...")
            break

        } catch (ex: ClosedByInterruptException) {
            Console.printInfo("\nPrzerwano przez użytkownika (Ctrl+C). Uruchamiam finalny zapis...")
            break

        } catch (ex: Throwable) {
            Console.printError("\nWystąpił nieoczekiwany błąd: ${ex.message}")
            ex.printStackTrace()
            break
        }
    }
}
