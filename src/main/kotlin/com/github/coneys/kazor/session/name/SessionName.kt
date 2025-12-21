package com.github.coneys.kazor.session.name

import com.github.coneys.kazor.llm.LlmClient
import com.github.coneys.kazor.session.SessionHistory

class SessionName(private val client: LlmClient) {
    fun generateFor(input: String): String {
        return client.createChatSession(
            systemInstruction = "Twoim zadaniem jest wygenerowanie nazwy dla sesji na podstawie pierwszej wiadomości która zostanie przesłana." +
                    " Powinna być krótka i zwięzła." +
                    " Ważne, żeby nie było tu żadnych znaczników HTML czy markdown." +
                    " Nie powinieneś dodawać żadnych swoich uwag czy opisów. To co zwrócisz w odpowiedzi stanie się nazwą sesji." +
                    " Ważne jednak żeby dodać nieco kontekstu, żebym mógł odróżnić od siebie sesje, wiec nie może to być jedno słowo (chyba, że rzeczywiście nie ma innej opcji)",
            history = SessionHistory.empty,
            options = LlmClient.Options.empty
        ).sendMessage(input).text
    }
}