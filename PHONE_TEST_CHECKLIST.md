# MAYA phone QA checklist (Android)

Install the **newest** GitHub Actions debug APK from the workflow artifact.
Debug APK build success is not sufficient evidence for full device behavior.

- [ ] Upgrade from previous MAYA Original (same package), confirm memories preserved.
- [ ] Open Settings and input your own Gemini API key. Do NOT paste keys into GitHub.
- [ ] Start Gemini Live, speak, interrupt; verify mic/speaker don't talk over each other.
- [ ] Test flash, volume, brightness with necessary Android permissions.
- [ ] Memory Packs → export JSON → import same file → check no duplicate entries.
- [ ] Save new memory, restart the app; confirm persistence.
- [ ] Enable Notification Access; use only a test message from a trusted contact;
      ensure the REVIEW and SEND confirmation is required.
- [ ] Choose contact, verify system dialer opens without automatically calling.
- [ ] Select each of the 17 shapes; return to main screen and verify selection persists.
- [ ] Create reminder for 2 minutes; inspect notification permission and timing.
- [ ] Reboot and test a second reminder (Android delivery may be inexact).
- [ ] Select a test screenshot/photo; confirm OCR text is local; do not test
      sensitive passwords or financial documents.
- [ ] Select a multipage PDF with Latin text; scan first 6 pages.
- [ ] Select a QR/barcode test image; confirm output without opening URLs automatically.
- [ ] Open App Macros → review sample script → remove any unfamiliar taps → manually
      approve; stop with Cancel if needed.
- [ ] Verify back/home/scroll support only when Accessibility is enabled.
- [ ] Check Diagnostics; compare permission flags to Android Settings.
- [ ] Test WhatsApp draft only; automatic delivery is NOT included.
- [ ] Note any crash, Android OS version and reproduction steps, without
      exposing private messages, contacts, API keys, or medical information.

Never assume 3D, offline hotword, hidden camera security, 24/7 voice, or
autonomous verified messaging exists in this debug build.
