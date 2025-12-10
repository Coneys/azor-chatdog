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
/audio            - Generuje plik WAV z ostatniej odpowiedzi asystenta (wymaga działającego serwera TTS — patrz sekcja „Audio”).
/audio-all        - Generuje jeden plik WAV z całej sesji (naprzemiennie TY ↔ asystent). Wymaga serwera TTS obsługującego wiele głosów — patrz sekcja „Audio”.

/session list     - Wyświetla listę dostępnych sesji.
/session display  - Wyświetla całą historię sesji.
/session pop      - Usuwa ostatnią parę wpisów (TY i asystent).
/session clear    - Czyści historię bieżącej sesji.
/session new      - Rozpoczyna nową sesję.
```

Dodatkowo możesz przekazać `--session-id=<ID>` przy uruchomieniu, aby od razu wejść do wskazanej sesji.


## Audio (/audio, /audio-all)
Funkcje audio pozwalają wygenerować plik(i) WAV z odpowiedzi asystenta.

- `/audio` — generuje plik WAV z ostatniej odpowiedzi asystenta i zapisuje go jako `~/.kazor/<SESSION_ID>-last-response.wav`.
- `/audio-all` — generuje jeden plik WAV z całej sesji (naprzemiennie: TY ↔ asystent) i zapisuje go jako `~/.kazor/<SESSION_ID>-whole-session.wav`.

Wymagania po stronie serwera TTS (HTTP):
- Endpoint: `POST /synthesize`
- Body: formularz `application/x-www-form-urlencoded` z polami:
  - `text` — treść do przeczytania (wymagane)
  - `model_id` — identyfikator wariantu głosu, jako string, np. `"0"` lub `"1"` (opcjonalne dla `/audio`, wymagane praktycznie dla `/audio-all`, aby rozróżnić głos asystenta i użytkownika)
- Odpowiedź: `audio/wav` (treść pliku WAV). Dla poprawnego łączenia przy `/audio-all` zalecany format: 22050 Hz, 16‑bit, mono, signed little‑endian.

Domyślna konfiguracja klienta w aplikacji:
- Host: `0.0.0.0`
- Port: `8000`
- Pełny URL: `http://0.0.0.0:8000/synthesize`

Użycie w czacie:
- `/audio` — aplikacja wyśle ostatnią odpowiedź modelu jako `text` do serwera TTS, a otrzymany WAV zapisze lokalnie jako `~/.kazor/<SESSION_ID>-last-response.wav`.
- `/audio-all` — aplikacja prześle po kolei treści wszystkich wypowiedzi z historii sesji. Dla wpisów asystenta wyśle `model_id=0`, a dla wpisów użytkownika `model_id=1`. Otrzymane WAV-y zostaną połączone w jeden plik `~/.kazor/<SESSION_ID>-whole-session.wav`.

Przykładowy serwer TTS z wieloma głosami (Python + FastAPI + Coqui TTS XTTS v2):

```python
from fastapi import FastAPI, Form, HTTPException
from fastapi.responses import FileResponse
from TTS.api import TTS
import tempfile
import os
import torch
from TTS.tts.configs.xtts_config import XttsConfig
from TTS.tts.models.xtts import XttsAudioConfig, XttsArgs
from TTS.config.shared_configs import BaseDatasetConfig
import traceback
from starlette.background import BackgroundTask

app = FastAPI()

torch.serialization.add_safe_globals([XttsConfig, XttsAudioConfig, BaseDatasetConfig, XttsArgs])

SAMPLE_AGENT_PATH = "samples/mine.wav"  # głos asystenta (model_id = "0")
SAMPLE_USER_PATH = "samples/other.wav"  # głos użytkownika (model_id = "1")

if not os.path.exists(SAMPLE_AGENT_PATH) or not os.path.exists(SAMPLE_USER_PATH):
    print("Błąd: Brak plików sampli głosowych! Ustaw poprawne ścieżki.")

try:
    XTTS_ENGINE = TTS(model_name="tts_models/multilingual/multi-dataset/xtts_v2")
except Exception as e:
    print(f"Błąd ładowania modelu XTTSv2: {e}")
    XTTS_ENGINE = None

@app.post("/synthesize")
async def synthesize(
        text: str = Form(...),
        model_id: str = Form("0")
):
    if XTTS_ENGINE is None:
        raise HTTPException(status_code=503, detail="Serwer TTS nie jest gotowy.")

    if model_id == "0":
        speaker_wav_path = SAMPLE_AGENT_PATH
    elif model_id == "1":
        speaker_wav_path = SAMPLE_USER_PATH
    else:
        raise HTTPException(
            status_code=400,
            detail="Nieprawidłowa wartość dla 'model_id'. Użyj '0' (Agent) lub '1' (Użytkownik)."
        )

    if not os.path.exists(speaker_wav_path):
        raise HTTPException(status_code=500, detail=f"Plik sampla głosowego nie został znaleziony: {speaker_wav_path}")

    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as tmp_file:
        try:
            XTTS_ENGINE.tts_to_file(
                text=text,
                file_path=tmp_file.name,
                speaker_wav=speaker_wav_path,
                language="pl"
            )
            tmp_filename = tmp_file.name
        except Exception as e:
            print("\nBŁĄD SYNTEZY TTS TRACEBACK")
            traceback.print_exc()
            print("---------------------------------")
            if os.path.exists(tmp_file.name):
                os.remove(tmp_file.name)
            raise HTTPException(status_code=500, detail=f"Błąd syntezy mowy: {e}")

    def cleanup():
        os.remove(tmp_filename)

    task = BackgroundTask(cleanup)

    response = FileResponse(
        tmp_filename,
        media_type="audio/wav",
        filename="output.wav",
        background=task
    )

    return response
```

Jeśli Twój serwer TTS działa pod innym adresem lub portem, zmodyfikuj wartości `host` i `port` w `AudioGenerator` lub przygotuj własną instancję klienta.

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