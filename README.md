# Azor Chatdog

Prosty, terminalowy chat z asystentem AI, z obsługą sesji i dwóch silników LLM: Gemini (Google) oraz Ollama (lokalny).


## Wymagania
- Java 19+ (projekt używa toolchainu JDK 19)
- Gradle (wrapper dołączony, więc wystarczy `./gradlew`)
- Dostęp do jednego z silników:
  - Gemini: wymagany klucz API Google AI (`GEMINI_API_KEY`)
  - Ollama: zainstalowany Ollama z pobranym modelem (np. `mistral`) i działający serwer na `http://localhost:11434`


## Szybki start (Gradle run)
Domyślny sposób uruchamiania:

```bash
./gradlew run
```

Aplikacja czyta z `stdin`, możesz więc pisać bezpośrednio w terminalu.

Przykład kontynuowania istniejącej sesji poprzez parametr CLI:

```bash
./gradlew run --args="--session-id=<TWOJE_SESSION_ID>"
```


## Konfiguracja silnika (ENGINE) i modelu
Silnik wybierany jest przez zmienną środowiskową `ENGINE`:
- `ENGINE=GEMINI` (domyślne)
- `ENGINE=OLLAMA`

Dodatkowe zmienne środowiskowe:
- `MODEL_NAME` — nazwa modelu (opcjonalne)
  - Gemini: domyślnie `gemini-2.5-flash`
  - Ollama: domyślnie `mistral`
- `GEMINI_API_KEY` — wymagany przy `ENGINE=GEMINI`

Przykłady uruchomienia:

- Gemini:
  ```bash
  ENGINE=GEMINI MODEL_NAME=gemini-2.5-flash ./gradlew run
  ```

- Ollama:
  ```bash
  ENGINE=OLLAMA MODEL_NAME=mistral ./gradlew run
  ```


## Silniki LLM
- Gemini (Google AI):
  - Klient inicjowany na podstawie `GEMINI_API_KEY`
  - Pliki: `src/main/kotlin/com/github/coneys/kazor/llm/gemini/*`
- Ollama (lokalny):
  - Wymaga działającego serwera na `http://localhost:11434`
  - Pliki: `src/main/kotlin/com/github/coneys/kazor/llm/ollama/*`


## Komendy (slash commands)
W trakcie czatu dostępne są następujące polecenia:

```
/switch <ID>      - Przełącza na istniejącą sesję.
/help             - Wyświetla tę pomoc.
/exit, /quit      - Zakończenie czatu.

/session list     - Wyświetla listę dostępnych sesji.
/session display  - Wyświetla całą historię sesji.
/session pop      - Usuwa ostatnią parę wpisów (TY i asystent).
/session clear    - Czyści historię bieżącej sesji.
/session new      - Rozpoczyna nową sesję.
```

Dodatkowo możesz przekazać `--session-id=<ID>` przy uruchomieniu, aby od razu wejść do wskazanej sesji.


## Gdzie zapisywane są sesje?
Pliki sesji zapisywane są w katalogu użytkownika:

- macOS/Linux: `~/.kazor`
- Windows: `C:\Users\<nazwa_uzytkownika>\.kazor`

Przykładowe pliki mają nazwę w formacie `<SESSION_ID>-log.json`.


## Wskazówki i przykłady
- Start nowej rozmowy z domyślnym silnikiem (Gemini):
  ```bash
  ./gradlew run
  ```
- Start z Ollama i własną nazwą modelu:
  ```bash
  ENGINE=OLLAMA MODEL_NAME=mistral ./gradlew run
  ```
- Kontynuacja konkretnej sesji:
  ```bash
  ./gradlew run --args="--session-id=abcd-1234"
  ```
- Wyświetlenie pomocy w trakcie czatu: wpisz `/help`


## Rozwiązywanie problemów
- „Brak odpowiedzi” przy Ollama: upewnij się, że serwer działa (`ollama serve`) i model jest pobrany (`ollama pull mistral`).
- Gemini zwraca błędy uwierzytelnienia: sprawdź, czy `GEMINI_API_KEY` jest ustawiony oraz ma ważne uprawnienia.
- Problemy z kolorami w terminalu: niektóre terminale mogą inaczej interpretować kody ANSI — to tylko kwestia wyświetlania.