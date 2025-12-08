package com.github.coneys.kazor.audio

import com.github.coneys.kazor.console.Console
import com.github.coneys.kazor.session.SessionSnapshot
import kotlinx.coroutines.runBlocking

fun recordAudioFromLastResponse(sessionSnapshot: SessionSnapshot) = runBlocking {
    val response = sessionSnapshot.history.lastAgentResponse()
    if (response == null) {
        Console.printError("Nie istnieje w histori odpowiedź modelu dla której można wygenerować dźwięk")
        return@runBlocking
    }

    val audio = AudioGenerator.default.from(response)
    if (audio == null) {
        Console.printError("Nie udało się wygenerować pliku audio dla \"$response\"")
        return@runBlocking
    }

    val savedAt = AudioStorage.storeLastSessionResponse(audio, sessionSnapshot.sessionId)
    Console.printInfo("Audio zostało zapisane w pliku: $savedAt")
}