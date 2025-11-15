package com.github.coneys.kazor.console

import com.github.coneys.kazor.session.SessionStorage


/**
 * Console output utilities for the chatbot.
 * Provides consistent colored terminal output.
 */
object Console {

    // ANSI Colors
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val CYAN = "\u001B[36m"
    private const val BLUE = "\u001B[34m"
    private const val YELLOW = "\u001B[33m"
    private const val WHITE_BOLD = "\u001B[97;1m"


    /** Print error in red */
    fun printError(message: String) {
        println("$RED$message$RESET")
    }

    /** Print assistant message in cyan */
    fun printAssistant(message: String) {
        println("$CYAN$message$RESET")
    }

    /** Print user message in blue */
    fun printUser(message: String) {
        println("$BLUE$message$RESET")
    }

    /** Print info (no color in Python version) */
    fun printInfo(message: String) {
        println(message)
    }

    /** Print help in yellow */
    fun printHelp(message: String) {
        println("$YELLOW$message$RESET")
    }

    /** Prints help block (equivalent of display_help in Python) */
    fun displayHelp(sessionId: String) {
        printInfo("Aktualna sesja (ID): $sessionId")
        printInfo("Pliki sesji są zapisywane na bieżąco w: ${SessionStorage.logDir}")

        displayAvailableCommands()
    }

    fun displayAvailableCommands() {
        printHelp("Dostępne komendy (slash commands):")
        printHelp("  /switch <ID>      - Przełącza na istniejącą sesję.")
        printHelp("  /help             - Wyświetla tę pomoc.")
        printHelp("  /exit, /quit      - Zakończenie czatu.")

        printHelp("\n  /session list     - Wyświetla listę dostępnych sesji.")
        printHelp("  /session display  - Wyświetla całą historię sesji.")
        printHelp("  /session pop      - Usuwa ostatnią parę wpisów (TY i asystent).")
        printHelp("  /session clear    - Czyści historię bieżącej sesji.")
        printHelp("  /session new      - Rozpoczyna nową sesję.")
    }

    /** Prints final instructions (same as Python's display_final_instructions) */
    fun displayFinalInstructions(sessionId: String) {
        printInfo("\n--- Instrukcja Kontynuacji Sesji ---")
        printInfo("Aby kontynuować tę sesję (ID: $sessionId) później, użyj komendy:")

        val command = "$WHITE_BOLD\n    java -jar <twoja-aplikacja.jar> --session-id=$sessionId\n$RESET"
        println(command)

        printInfo("--------------------------------------\n")
    }

    private val DOG_ART_BASE_RAW = """
           ,////,
          /  ' ,)
         (ò____/
        /  ~ \
       |  /   `----.
       | |         |
      /   \        |
     ~   / \
    ~   |   \
       /     \
      '       '
""".trimIndent()

    fun printAssistantImage(text: String): String {
        val textLength = text.length
        val top = "   " + "-".repeat(textLength + 2) + "."
        val middle = "  ( $text )"
        val bottom = "   " + "-".repeat(textLength + 2) + "'"
        val tail1 = "      \\"
        val tail2 = "       \\"

        return listOf(
            top,
            middle,
            bottom,
            tail1,
            tail2,
            DOG_ART_BASE_RAW
        ).joinToString("\n")
    }


    fun printWelcome() {
        try {
            println(printAssistantImage("Woof Woof!"))
        } catch (e: Exception) {
            System.err.println("Unknown Error.")
        }
    }

}
