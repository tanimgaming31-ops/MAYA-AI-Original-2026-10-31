package com.tanimworld.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity implements MayaBus.Listener {
    private TextView status, transcript;
    private OrbView orb;
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(0xFF060B1E);
        ScrollView root = new ScrollView(this); root.setFillViewport(true); root.setBackgroundColor(0xFF060B1E);
        LinearLayout col = new LinearLayout(this); col.setPadding(28, 42, 28, 28); col.setOrientation(LinearLayout.VERTICAL); col.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(col);
        TextView title = label("MAYA", 36, 0xFF7DF5F7); col.addView(title);
        TextView subtitle = label("AI VOICE ASSISTANT   •   TaNiM world", 13, 0xFFACB6D0); col.addView(subtitle);
        orb = new OrbView(this); col.addView(orb, new LinearLayout.LayoutParams(-1, 305));
        status = label("OFFLINE • TAP START", 17, Color.WHITE); col.addView(status);
        transcript = label("Your original AI companion. Start microphone for Gemini Live, or use local commands below.", 14, 0xFF9BA9C5); col.addView(transcript);
        row(col, "START VOICE", () -> startVoice(), "STOP", () -> stopVoice());
        row(col, "SETTINGS", () -> startActivity(new Intent(this, SettingsActivity.class)), "MEMORIES", () -> say(CommandRouter.recall(this, "")));
        row(col, "TORCH ON", () -> { if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.CAMERA}, 13); } else say(CommandRouter.torch(this, true)); }, "TORCH OFF", () -> say(CommandRouter.torch(this, false)));
        row(col, "OPEN CAMERA", () -> say(CommandRouter.openApp(this, "camera")), "READ SCREEN", () -> say(CommandRouter.inspectScreen()));
        row(col, "MEMORY BACKUP", () -> startActivity(new Intent(this, MemoryPackActivity.class)), "CONTACT PICKER", () -> startActivity(new Intent(this, ContactPickerActivity.class)));
        row(col, "NOTIFICATION REPLIES", () -> startActivity(new Intent(this, ReplyActivity.class)), "DIAGNOSTICS", () -> startActivity(new Intent(this, DiagnosticsActivity.class)));
        row(col, "17 AVATARS", () -> startActivity(new Intent(this, AvatarPickerActivity.class)), "SCREENSHOT", () -> say(ScreenshotHelper.capture()));
        row(col, "DOCUMENT OCR", () -> startActivity(new Intent(this, DocumentToolsActivity.class)), "QR / BARCODE", () -> startActivity(new Intent(this, DocumentToolsActivity.class)));
        EditText command = new EditText(this); command.setSingleLine(false); command.setMinLines(2);
        command.setHint("Try: remember my cat is black\nopen youtube · torch on · search weather");
        command.setHintTextColor(0xFF68758D); command.setTextColor(Color.WHITE);
        col.addView(command, new LinearLayout.LayoutParams(-1, -2));
        row(col, "RUN LOCAL", () -> say(CommandRouter.runText(this, command.getText().toString())),
                "ASK GEMINI", () -> {
                    String text = command.getText().toString().trim();
                    if (text.isEmpty()) { say("Type a question."); return; }
                    if (VoiceService.active == null) { say("Start Voice first."); return; }
                    VoiceService.active.sendText(text); say("Sent text to Gemini.");
                });
        TextView footer = label("Original independent source • Not a copy of Playlist\nVoice needs your own Gemini API key and internet access.", 12, 0xFF8190AA);
        col.addView(footer);
        setContentView(root);
        MayaBus.listen(this);
    }
    private TextView label(String text, int size, int color) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(color); v.setTextSize(size);
        v.setGravity(Gravity.CENTER); v.setPadding(0, 8, 0, 12); return v;
    }
    private void row(LinearLayout col, String a, Runnable left, String b, Runnable right) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        Button l = new Button(this); l.setAllCaps(false); l.setText(a); l.setOnClickListener(v -> left.run());
        Button r = new Button(this); r.setAllCaps(false); r.setText(b); r.setOnClickListener(v -> right.run());
        row.addView(l, new LinearLayout.LayoutParams(0, -2, 1)); row.addView(r, new LinearLayout.LayoutParams(0, -2, 1));
        col.addView(row);
    }
    private void say(String message) { transcript.setText(message); }
    private void startVoice() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 12); return;
        }
        Intent intent = new Intent(this, VoiceService.class); intent.setAction(VoiceService.START);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
    }
    private void stopVoice() {
        Intent intent = new Intent(this, VoiceService.class); intent.setAction(VoiceService.STOP); startService(intent);
        status.setText("STOPPED"); orb.setState("IDLE");
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(requestCode, permissions, grants);
        if (requestCode == 12 && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) startVoice();
        if (requestCode == 13 && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) say(CommandRouter.torch(this, true));
    }
    @Override public void onEvent(String state, String detail) {
        if (status != null) { status.setText(state); if (detail != null && !detail.isEmpty()) transcript.setText(detail); orb.setState(state); }
    }
    @Override protected void onResume() { super.onResume(); MayaBus.listen(this); }
    @Override protected void onPause() { super.onPause(); MayaBus.listen(null); }
}
