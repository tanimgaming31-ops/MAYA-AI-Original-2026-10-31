# MAYA AI Voice Assistant — TaNiM world (independent Android source)

This is a **fresh, original codebase**, NOT a copy, fork or derivative repository
of Tech GPTx / Playlist. Android native application, Java 17, Android 8.0+.

## Currently coded (still requires a real APK build + Android-device testing)
- Real-time Gemini Live WebSocket microphone 16kHz PCM, streamed 24kHz playback,
  interruption handling, dynamic model selection, customizable language/voice.
- User-provided Gemini key stored locally via Android Keystore AES-GCM.
- Text and voice-driven Gemini tool calls for flashlight, volume, brightness,
  app launch, web search, save/read memories, screen inspection, deliberate
  accessibility click/text and YouTube.
- Foreground notification-backed mic service, explicit start and stop.
- Local SQLite memory DB with add/search/list/delete via command routing.
- User-requested Android 11+ screenshot save via Accessibility, plus simple
  best-effort local reminders (delivery may be delayed).
- Built-in animated orb with listening/speaking states, dark UI, Settings.
- Optional accessibility, enabled explicitly by the phone owner, with sensitive
  input blocking and no automatic installation/permissions or mass actions.
- WhatsApp **draft/open** flow only; sending is an intentional user action.
- Android debug APK GitHub Actions workflow using free hosted runner minutes
  where available; no computer required to upload via GitHub mobile/web.

## NOT implemented / not verified
See FEATURE_STATUS.md. This is NOT complete feature parity with Playlist Part 7;
all-device control, reliable background wake word, complete Instagram/Spotify
workflows, video screen sharing, screen-image reasoning, biometrics, advanced 3D avatar,
full autonomous agent, calls and automated WhatsApp sending require dedicated
native implementation, service permissions, and device tests.

## Build from an Android phone using GitHub
1. In GitHub, create an **empty repository** you own, e.g. `MAYA-AI-Original`.
   Do NOT initialize it with README, as this archive already has one.
2. On Android, download this ZIP to Downloads. In Termux (obtain from a trusted
   Termux distribution), install Git and unzip. Enable shared-storage access.
3. Run these commands in Termux, adapting the ZIP name and GitHub username:

   ```sh
   pkg update
   pkg install git unzip
   termux-setup-storage
   cd ~/storage/downloads
   unzip MAYA_AI_Voice_Assistant_TaNiM_world_Original_Source.zip
   cd MAYA_AI_Independent
   git init -b main
   git config user.name "TaNiM world"
   git config user.email "YOUR_OWN_GITHUB_EMAIL"
   git add .
   git commit -m "Independent MAYA Android starter"
   git remote add origin https://github.com/YOUR_GITHUB_USERNAME/MAYA-AI-Original.git
   git push -u origin main
   ```

   GitHub HTTPS pushes require a personal access token / authentication rather
   than a GitHub account password. Never commit any credential or Gemini key.
   If you already know how to upload folders while preserving `.github/`, you
   may use GitHub's website instead of Termux.
4. Open **Actions → Build MAYA Android APK → Run workflow**. A push to `main`
   also starts the workflow.
5. Open a completed workflow → **Artifacts → MAYA-TANIM-DEBUG-APK**.
6. Download the artifact, extract `app-debug.apk`, and install on Android.
7. In the app Settings enter your own Gemini API key, grant microphone permission,
   and begin a voice session. Voice tool calling depends on model support.

The debug APK is **debug-signed**, not an independently verified release binary.
For publication, configure your own protected release keystore separately.

## Privacy and limitations
- Microphone use is initiated by an explicit button, with a persistent
  notification. Android can restrict background microphone service startup.
- Voice audio is transmitted to Google's Gemini API during an active session.
- API keys used directly on-device carry extraction risk on compromised phones;
  production applications should use server-minted ephemeral Live tokens.
- Gemini model names and pricing can change. Change the model in Settings.
- Accessibility reads the active Android accessibility tree when triggered.
  It does not silently grant permissions, install apps, or bypass OS security.
- All voice actions have real Android limitations and may fail on a given ROM.

## Project ownership
See LICENSE.txt and THIRD_PARTY_NOTICES.md. New original code is labeled
Copyright (c) 2026 TaNiM world. That does not erase any third-party legal rights.

## MAYA new native utilities (2026-10-31)

The Android source now includes local Memory Pack JSON export/import, Android
Notification Listener with **per-message send confirmation**, contact picker
and dialer, persisted reminders after reboot, 17 newly drawn 2D avatars, safe
bounded macros, native diagnostics and user-selected image/PDF OCR and QR/barcodes.

- Open MAYA's main screen, use **MEMORY BACKUP**, **NOTIFICATION REPLIES**,
  **DIAGNOSTICS**, **17 AVATARS**, **DOCUMENT OCR**, and **APP MACROS**.
- Set permission/access manually in Android Settings; MAYA never silently does it.
- The OCR library is Google ML Kit, which carries its own license and models.
- APK Build: Actions > Bootstrap and build original MAYA Android app > latest
  successful run > Artifacts > MAYA-TaNiM-world-debug-apk.
- All code is newly authored for MAYA; no Tech GPTx source/assets were copied.
- See `FEATURE_STATUS.md` and `PHONE_TEST_CHECKLIST.md` for honest limitations.
