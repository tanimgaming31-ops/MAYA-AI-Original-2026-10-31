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


## v0.4 Pastel home and UI navigation — 2026-10-31

- Commit built: `62130e6b90b267efecfdcf1d0d08ca21c9eee4e4`
- GitHub Actions run: https://github.com/tanimgaming31-ops/MAYA-AI-Original-2026-10-31/actions/runs/41170147146
- Outcome: **SUCCESS** — source verified, Java/Android SDK setup, Gradle assembleDebug, Artifact upload, Release upload.
- APK size: **68,369,145 bytes**
- APK SHA256: `d18c236a3f12589cd871cb3f48a8faf1fd94b8f7c66bb019289e2e1919b1a500`.
- ZIP / APK archive integrity: passed (classes.dex / classes2.dex / classes3.dex present).
- Main app version: `0.4.0-original-pastel`, versionCode `4`, package `com.tanimworld.maya`.
- New native screens: original pastel home, rotating particle sphere, opt-in Open-Meteo location weather card,
  live voice control, quick Screen/Memory/Language cards, Home/Chat/Mic/Settings/Tools bottom bar,
  full Tools screen retaining older controls, and chat entry.
- All older core files (MemoryStore, VoiceService, Gemini Live interface, Android notification replies, OCR,
  scanner, macros, other tools and local memory data format) retained.
- **Not hardware tested:** voice reliability, location permission/weather request, button hit targets on each
  screen size, and memory continuity on actual user phone.
- IMPORTANT: The existing GitHub Release tag and asset filename currently say `v0.3`, but the artifact there
  was freshly replaced by the v0.4 build. Match its SHA256 against the hash above; do not rely on the label.
  A new standalone v0.4 release tag was not created.
- This is an original layout recreation based on high-level user-provided design direction only; no copied
  Jerry UI source, art, logos or licensed assets.
