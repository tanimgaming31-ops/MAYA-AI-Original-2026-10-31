# MAYA AI Voice Assistant — feature status
Updated 2026-10-31, developer/publisher label: **TaNiM world**.

This is an independently authored Android app, **not** a renamed copy of Playlist.
**BUILT** means GitHub Actions compilation succeeded for code in this repository,
not that it has passed real Android hardware tests.
**CODE** means implementation exists and requires APK/device testing.
**LIMITED** means only partial behavior is provided.
**PLANNED** means not implemented.

| Feature | Status | What you should expect |
|---|---|---|
| Gemini Live native 16kHz/24kHz audio | CODE | Live network session; needs user's API key and real phone tests |
| Voice-to-Android function calling | CODE | Tool schemas for safe controls, still needs real use validation |
| Barge-in / interruption | LIMITED | Playback interrupt event; no measured audio-focus reliability |
| Local memory storage/retrieval | CODE | SQLite facts and lookup |
| **Memory Pack export and merge** | CODE | System file picker; JSON backup/import; preserves existing facts |
| Flashlight, brightness, volume | CODE | Android permission/hardware restrictions |
| Any installed app by accessible launcher name | LIMITED | Android app visibility and clone packages can differ |
| Browser/YouTube search | LIMITED | Opens results, not automatic video play |
| WhatsApp draft | LIMITED | Requires phone number; opens draft, user manually sends |
| **Notification quick reply** | CODE | User enables notification access and confirms every Send; delivery unverified |
| **Contact picker** | CODE | User chooses number, dialer opens, no automatic call |
| **17 original vector avatars** | CODE | 2D animated shapes, selectable and available in an opt-in overlay (not 3D models) |
| **Device Diagnostics** | CODE | Permissions, service availability, key presence |
| **App navigation/media controls** | CODE | Back/home/scroll, play/pause/next/previous; UI outcomes not guaranteed |
| **Bounded App Macros** | LIMITED | User reviews each run; limited steps, protected actions blocked |
| **Image and PDF OCR** | CODE | User-selected file, ML Kit Latin-script text; PDF first six pages |
| **QR/barcode scanner** | CODE | Reads user-selected image, not a live camera scanner |
| **Receipt subtotal/total hint** | LIMITED | Simple total text extraction; not validated expense accounting |
| User-requested screenshot | CODE | Accessibility screenshot, Android 11+; needs phone test |
| Screen text inspection | CODE | Limited accessible visible UI tree; passwords excluded |
| **Optional floating avatar overlay** | CODE | User explicitly grants Draw over other apps; no automatic microphone use |
| **Explicit-consent Gemini image vision** | CODE | User chooses photo/screenshot and approves upload to Google; Gemini response needs verification |
| Reboot-persistent inexact reminders | CODE | Restores scheduled notifications after reboot; Android may delay |
| Always-on offline wake word / Vosk | PLANNED | No bundled model or offline hotword |
| All-day unattended background listening | PLANNED | Android foreground session only |
| Screen MediaProjection to Gemini Live | PLANNED | No continuous video/screen-share integration |
| Full agentic multi-app action planning | PLANNED | No closed-loop planner / verification |
| Spotify/Instagram autonomous workflows | PLANNED | No reliable dedicated integration |
| Fully rigged 3D girl, hair/eye/lip sync | PLANNED | Vector avatars are not 3D |
| Owner voice recognition | PLANNED | No biometric speaker identification |
| Intruder face detection/camera alerts | PLANNED | No hidden camera behavior |
| Full handwriting/medicine multilingual OCR | PLANNED | Current OCR is Latin text with manual review |
| Phone hardware integration tests | PLANNED | No user-phone tests yet |
| Production signature/Play Store release | PLANNED | Debug APK only |

**License reality:** TaNiM world owns the newly authored application-specific files
to the extent applicable law recognizes that ownership. Bundled Android SDK, OkHttp
and Google ML Kit have separate licenses and must retain their original rights.
Do not represent this project as having no third-party licenses.

**Source and builds:** https://github.com/tanimgaming31-ops/MAYA-AI-Original-2026-10-31

## v0.4 UI Redesign

| Feature | Status | Reality |
|---|---|---|
| New warm-pastel MAYA home | CODE | Native Android card layout, original style, hardware QA pending |
| Particle sphere with rotating visual | CODE | Native Canvas 3D-projected 2D particles; does not claim rigged 3D model |
| Local weather | LIMITED | Live Open-Meteo only after optional Android coarse location permission; no location = no invented temperature |
| Bottom navigation | CODE | Home, Chat, Mic, Settings and Tools screens |
| All older shortcuts preserved | CODE | Moved to Tools; not removed from project |
| Original logo/branding | CODE | MAYA by TaNiM world; no copied Jerry assets |
