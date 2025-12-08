package com.github.coneys.kazor.audio

import java.nio.file.Paths

object AudioStorage {
    val logDir = Paths.get(System.getProperty("user.home"), ".kazor").toFile().also { it.mkdirs() }

    fun storeLastSessionResponse(audio: ByteArray, sessionId: String) =
        logDir.resolve("${sessionId}-last-response.wav").also { it.writeBytes(audio) }
}