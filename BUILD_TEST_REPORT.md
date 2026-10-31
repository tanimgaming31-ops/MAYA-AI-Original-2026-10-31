# Build and verification report — 2026-10-31

## Completed checks
- Independently authored and packaged 12 Android Java source files.
- All Android XML files passed local XML parsing.
- Java source was passed to a local `javac` parsing pass; the environment lacks
  Android SDK / Android dependencies, so symbol resolution and actual Android
  compilation could not be completed. No Java lexical/syntax errors appeared
  in the local diagnostic output before unresolved Android API symbols.
- Gradle settings, application module, GitHub Actions YAML, package names,
  explicit user-permission descriptions, and feature-status documentation exist.
- License and third-party notices are provided separately.

## Not yet verified
- **No Android APK has been compiled here.**
- GitHub Actions has not been run for this independent project.
- No phone installation, Gemini WebSocket handshake, microphone/audio playback,
  device action, or Accessibility behavior has been tested.
- Automated tests / Android instrumented tests are not supplied in this starter.

## How to produce a real proof
1. Upload source as a new GitHub repo owned by TaNiM world.
2. Run the provided `Build MAYA Android APK` action.
3. Investigate any Gradle/compiler warnings or errors reported by GitHub Actions.
4. Install the debug APK on the target Android phone and test permissions,
   device actions, Live API, screenshots, and local reminders independently.
5. Keep debug/test reports with the repo before treating any feature as verified.

## v0.3 — 2026-10-31 — GitHub Actions verified build

- Source commit: `91b932eb23249d146a473888620af8ac58fdfa64`
- GitHub Actions run: https://github.com/tanimgaming31-ops/MAYA-AI-Original-2026-10-31/actions/runs/41168955944
- Conclusion: **success** — Android SDK setup, Gradle debug assemble and APK artifact upload all succeeded.
- Artifact: `MAYA-TaNiM-world-debug-apk`, ZIP containing `app-debug.apk`.
- SHA-256 of downloaded APK: `9a63d72501b7baa538c55f9dff722127ab0eb01be60adb9b2d3ea1566672e6e7`
- Artifact package size: 68,352,689 bytes (debug APK includes local OCR and barcode ML models).
- APK ZIP integrity check: passed; manifest and multidex payload exist.
- Added native source: memory JSON pack, approved notification reply, contact-picker/dial,
  diagnostics, 17 original 2D avatars and opt-in floating avatar, persistent reminders,
  bounded app macros, PDF/photo OCR, receipt total hint, barcode scan and consent-only image vision.
- **NOT tested:** hardware microphone audio quality, real Gemini API authentication,
  Messenger/WhatsApp app behavior, phone background restrictions, contact/data privacy
  behavior in runtime, all Android OEM variations, and notifications on a physical phone.
- **NOT completed:** full Playlist feature parity, true 3D facial animation, offline Vosk
  wake-word, live MediaProjection screen sharing, complete autonomous agent or biometric security.
- Licensing: newly authored source owned by TaNiM world; third-party SDK, OkHttp,
  ML Kit and other dependencies retain applicable separate rights.
