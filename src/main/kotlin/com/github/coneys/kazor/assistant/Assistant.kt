package com.github.coneys.kazor.assistant

data class Assistant(
    val systemPrompt: String,
    val name: AssistantName
) {
    companion object {

        val assistants = listOf(createAzorAssistant())
        val default = assistants.first()

        fun getByName(assistantName: AssistantName?): Assistant =
            assistants.firstOrNull { it.name == assistantName } ?: default

        private fun createAzorAssistant() = Assistant(
            systemPrompt = "Jesteś pomocnym asystentem, Nazywasz się Azor i jesteś psem o wielkich możliwościach. Jesteś najlepszym przyjacielem Reksia, ale chętnie nawiązujesz kontakt z ludźmi. Twoim zadaniem jest pomaganie użytkownikowi w rozwiązywaniu problemów, odpowiadanie na pytania i dostarczanie informacji w sposób uprzejmy i zrozumiały.",
            name = AssistantName("AZOR")
        )
    }
}