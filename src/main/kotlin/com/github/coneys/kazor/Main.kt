package com.github.coneys.kazor

import com.github.coneys.kazor.chat.mainLoop
import com.github.coneys.kazor.console.getSessionIdFromCli
import com.github.coneys.kazor.commands.CommandHandler
import com.github.coneys.kazor.console.Console
import com.github.coneys.kazor.session.SessionManager

fun main(args: Array<String>) {
    Console.printWelcome()
    val sessionId = getSessionIdFromCli(args)

    SessionManager.initializeFromCli(sessionId)

    mainLoop(CommandHandler()) { readlnOrNull() }

    Runtime.getRuntime().addShutdownHook(Thread {
        SessionManager.cleanupAndSave()
    })
}