package com.github.coneys.kazor.audio

import com.github.coneys.kazor.audio.AudioGenerator.Model
import com.github.coneys.kazor.console.Console
import com.github.coneys.kazor.message.Message
import com.github.coneys.kazor.session.SessionSnapshot
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.SequenceInputStream
import java.util.Collections
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream


private val VITS_FORMAT = AudioFormat(
    22050.0f,  // Częstotliwość próbkowania (Sample Rate)
    16,  // Głębia bitowa (Sample Size in Bits)
    1,  // Kanały (Channels - 1 dla Mono)
    true,  // Znakowe (Signed)
    false // Big Endian (False oznacza Little Endian, standard dla WAV)
)

fun recordAudioForWholeSession(sessionSnapshot: SessionSnapshot) = runBlocking {
    val entries = sessionSnapshot.history.entries

    if (entries.isEmpty()) {
        Console.printError("Nie istnieje rozmowa na podstawie której można wygenerować dźwięk")
        return@runBlocking
    }

    val generator = AudioGenerator.default

    val allFiles = entries.mapNotNull {
        val message = it.messages.filterIsInstance<Message.Text>().joinToString { it.text }
        generator.from(message, if (it.role == "model") Model.First else Model.Second)
    }

    println("Merging audio files... $allFiles")
    val combinedStream = mergeAudioBytesToSingleStream(allFiles) ?: return@runBlocking

    val savedAt = AudioStorage.storeWholeSession(combinedStream, sessionSnapshot.sessionId)
    Console.printInfo("Audio zostało zapisane w pliku: $savedAt")
}

fun mergeAudioBytesToSingleStream(audioChunks: List<ByteArray>): AudioInputStream? {
    // 1. Utwórz listę AudioInputStreams z każdej tablicy bajtów
    val audioStreams = audioChunks.map { audioData ->
        // Oblicz liczbę ramek na podstawie długości tablicy i rozmiaru ramki
        val frameLength = audioData.size.toLong() / VITS_FORMAT.frameSize

        // Utwórz AudioInputStream z ByteArrayInputStream i zdefiniowanego formatu
        AudioInputStream(ByteArrayInputStream(audioData), VITS_FORMAT, frameLength)
    }

    if (audioStreams.isEmpty()) {
        Console.printError("Brak danych audio do połączenia.")
        return null
    }

    // 2. Utwórz SequenceInputStream, aby połączyć wszystkie strumienie
    // SequenceInputStream przyjmuje Enumeration, a nie List, więc musimy to przekonwertować.
    val combinedStream = SequenceInputStream(Collections.enumeration(audioStreams))

    // 3. Utwórz nowy AudioInputStream, który zawiera wszystkie połączone dane
    // Całkowita długość w ramkach to suma długości wszystkich ramek
    val totalFrameLength = audioStreams.sumOf { it.frameLength }

    val combinedAudioInputStream = AudioInputStream(
        combinedStream,
        VITS_FORMAT,
        totalFrameLength
    )

    return combinedAudioInputStream
}