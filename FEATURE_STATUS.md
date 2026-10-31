# Feature audit (2026-10-31)

Legend: **SOURCE** = original implementation written, not APK-tested; **LIMITED**
= partial implementation; **NOT YET** = not claimed functional.

| Feature | Status | Reality |
|---|---|---|
| Gemini Live 16k/24k audio | SOURCE | Needs API/model/device test; API key required |
| Natural barge-in | LIMITED | Playback flush event; device echo/audio-focus needs test |
| Local memories | SOURCE | SQLite CRUD; no semantic retrieval embeddings |
| Voice tool calling | SOURCE | Uses supported model functionDeclarations, device test pending |
| Flash/volume/brightness | SOURCE | Android permission and OEM restrictions apply |
| Open apps/search web/YouTube | SOURCE | Best-effort intents; YouTube auto-play not assured |
| WhatsApp messaging | LIMITED | Opens prefilled draft; user sends manually |
| Android Accessibility | LIMITED | Explicit screen inspection, text/click by target label |
| Multi-step agent | NOT YET | No autonomous verified closed-loop general agent |
| Offline wake word | NOT YET | Needs permission-aware Vosk integration/model |
| Reliable hands-free all-day background | NOT YET | Foreground active call only, no offline standby |
| Screen sharing to Gemini | NOT YET | MediaProjection capture/stream not built |
| User-requested screenshot | SOURCE / LIMITED | Accessibility screenshot on Android 11+ saved to Pictures/MAYA; not device-tested |
| Live screenshot vision | NOT YET | Image analysis by Gemini not implemented |
| Spotify/Instagram automation | NOT YET | No reliable dedicated integration |
| 17 floating avatars, 3D model | NOT YET | Custom original canvas orb only |
| Owner voice biometrics | NOT YET | No speaker verification; not security-grade |
| Face detection/intruder photo | NOT YET | Not included |
| Notification quick replies | NOT YET | Not implemented |
| App macros & multi-app routines | NOT YET | Not implemented |
| PDF/receipt/medicine scanning | NOT YET | Not implemented |
| Reminders | SOURCE / LIMITED | Inexact AlarmManager notification; not reboot-persistent; device test pending |
| Clock alarm integration | NOT YET | Not implemented |
| GitHub Android APK workflow | SOURCE | Workflow present; cloud build not yet run |
| Production release signing | NOT YET | Debug artifact only |

This status file intentionally avoids claiming that code existence equals verified behavior.
