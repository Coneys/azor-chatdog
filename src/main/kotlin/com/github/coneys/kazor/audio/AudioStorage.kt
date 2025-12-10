package com.github.coneys.kazor.audio

import java.nio.file.Paths
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

object AudioStorage {
    val logDir = Paths.get(System.getProperty("user.home"), ".kazor").toFile().also { it.mkdirs() }

    fun storeLastSessionResponse(audio: ByteArray, sessionId: String) =
        logDir.resolve("${sessionId}-last-response.wav").also { it.writeBytes(audio) }

    fun storeWholeSession(combinedStream: AudioInputStream, sessionId: String) =
        logDir.resolve("${sessionId}-whole-session.wav").also { file ->
            AudioSystem.write(combinedStream, AudioFileFormat.Type.WAVE, file)
        }
}