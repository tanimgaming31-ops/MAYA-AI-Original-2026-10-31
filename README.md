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

### New v0.3 image vision and floating avatar

- **Floating MAYA ON** from main screen → Android overlay permission prompt → visible 2D floating avatar. Use **FLOATING OFF** to stop. This is not a hidden tracking service.
- **SCREENSHOT VISION** → choose an image saved on your device → REVIEW AND ASK GEMINI. The image is sent to Google's Gemini service only after a separate on-screen confirmation. A model response is not a medical diagnosis and may be wrong.
- New native ML Kit OCR/barcodes require their third-party licenses and can increase APK size.
- Debug APK build success does not imply phone-tested functionality.

## MAYA v0.4 — Original Pastel Home Redesign

The main screen is a **new native Android design**, inspired only by the user's requested general layout direction; no source, brand art, particles, graphics, third-party UI or voice files are copied from Jerry/Playlist.

- **MAYA**, Developer **TaNiM world**, original coral-and-lavender animated particle orb.
- Greetings based on the device's time; tap the large orb or central microphone to start/stop an active Gemini Live session.
- Optional **local-weather** card uses Open-Meteo weather readings, only after asking for coarse Android location permission. No simulated default temperatures. Requires network and enabled Location.
- Shortcuts for screen reading/screenshots, Memories/backup and Language/Settings.
- Fixed bottom navigation: Home, Chat, Mic, Settings, **Tools**.
- **Tools** keeps all previous features accessible: torch, camera, screen inspection, screenshot vision, ML Kit OCR, barcode, macros, notification replies, Memory Packs, diagnostics, floating MAYA, media and navigation.
- **Chat** retains local voice-independent text commands, plus text sent to the live Gemini session when connected.
- User data and prior MAYA Original memory remain in app-private storage. This is the same package name (com.tanimworld.maya); release/debug signature differences may affect whether Android allows an update, so back up Memory Packs first.
- Open-Meteo attribution: https://open-meteo.com/ . Weather is an optional live network request.

**Known limitations:** This is a visual/UI redesign, NOT evidence of Playlist feature parity. No automatic WhatsApp sending, genuine screen sharing or 3D human rig is claimed. A successful cloud compile is not the same as a physical Android phone test.
