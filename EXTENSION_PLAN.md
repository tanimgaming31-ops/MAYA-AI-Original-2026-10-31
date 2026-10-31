# Remaining work to approach Playlist functional parity

After current independently written MAYA features, the following require
substantial implementation, test devices, assets or explicit user approval:

1. Native offline Vosk wake word, licensed downloaded model, wake-word training,
   power budget and background service restrictions on Android 13–17.
2. Continuous Gemini Live screen video, MediaProjection consent lifecycle,
   sensitive screen masking, secure video frame ingestion and pause indicator.
3. Real 3D rigged avatar with licensed GLB/VRM model, blend-shapes, visemes,
   speech-aligned facial animation and rig tests.
4. Automated WhatsApp/IMO/Messenger contact search/send with per-send review,
   clone-package variations, UI verification and regression tests.
5. Reliable closed-loop AI agent: explicit goal, action plan, observation,
   safety boundaries, max attempts, user cancel, final-state verification.
6. Owner speaker enrollment and device-local embeddings; security evaluation
   before claiming biometric voice identification.
7. Optional visible camera-based owner checks with user consent and privacy.
   Covert capture and hidden monitoring are intentionally not implemented.
8. Full PDF/document/receipt toolkit: deskew, multilingual handwriting,
   expense database, receipt line-item extraction, privacy-aware backup.
9. Real notification inbox/safe response workflows beyond quick-reply actions;
   media/YouTube control validation; Spotify and Instagram compatibility.
10. Android emulator/physical phone tests, accessibility-specific tests,
    dependency/license audit, publishable signed APK/AAB.

A feature is not complete until it passes device tests on target Android versions.
Do not silently change other Maya, Zoya, Jeery or keyboard repositories.
