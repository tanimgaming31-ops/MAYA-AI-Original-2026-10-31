# Complete-parity work still required (not yet implemented)

The goal is to independently develop MAYA with comparable functionality to
JARVIS/Playlist Parts 1–7 *without copying source code or protected assets*.
This starter is only Stage 1; do not advertise comprehensive feature parity.

## Stage 2 — voice reliability
- Add offline Vosk hotword with model license verification and low-power service.
- Wake from notification / user action according to Android background rules.
- Bluetooth routing, audio focus, headset barge-in, detailed diagnostics,
  seamless microphone restart, and independent device QA.

## Stage 3 — Android and messaging
- Native scoped contact search, opt-in call intents, scheduled alarms and
  persistent reminders, notification listener with approved quick-reply.
- App macros with explicit per-app allowlist and testable UI verification.
- WhatsApp and other messaging workflows with per-send confirmation, no bypass.

## Stage 4 — visual reasoning and automation
- MediaProjection consent flow, properly managed screen-share service, frame
  throttling, redaction of sensitive content, Gemini video input.
- Agent plans/steps, inspection, state signatures, bounded retries, cancellation,
  cross-app action verification, consistent error reports.
- Targeted Instagram / Spotify tasks with explicit end-state validation.

## Stage 5 — polish and data
- 17 *new original* designed floating avatars, original Three.js animation,
  custom themes, avatar picker, accessibility HUD.
- Memory packs with JSON import/export, backups, multi-language translator,
  optional on-device owner voice recognition, accessibility diagnostics.
- Document/receipt/PDF scanning and expense tools with on-device processing.
- Security checks with explicit user opt-in; never silently photograph others.

## Stage 6 — shipping
- Android integration tests and emulator/phone smoke tests on representative
  Android 11–17 phones, third-party notice audit, privacy statement, release
  signing key under sole publisher control, update testing.
