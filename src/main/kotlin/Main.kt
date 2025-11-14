import chat.mainLoop
import console.getSessionIdFromCli
import commands.CommandHandler
import console.Console
import session.SessionManager

fun main(args: Array<String>) {
    Console.printWelcome()
    val sessionId = getSessionIdFromCli(args)

    SessionManager.initializeFromCli(sessionId)

    mainLoop(CommandHandler()) { readlnOrNull() }

    Runtime.getRuntime().addShutdownHook(Thread {
        SessionManager.cleanupAndSave()
    })
}