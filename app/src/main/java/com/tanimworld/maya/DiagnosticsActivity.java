package com.tanimworld.maya;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Observable settings diagnostics; no claim that untested actions work. */
public final class DiagnosticsActivity extends Activity {
    @Override public void onCreate(Bundle state) { super.onCreate(state); render(); }
    private void render() {
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(0xff070b1d);
        LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setPadding(26, 32, 26, 30);
        scroll.addView(col);
        String[] checks = {
            "Android SDK: " + Build.VERSION.SDK_INT,
            "Mic permission: " + yes(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED),
            "Camera permission: " + yes(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED),
            "Notification permission: " + (Build.VERSION.SDK_INT >= 33 ? yes(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) : "Pre-Android 13"),
            "Accessibility connected: " + yes(MayaAccessibilityService.active != null),
            "Notification Listener connected: " + yes(NotificationReplyService.active != null),
            "Gemini API key present: " + yes(SecretStore.hasApiKey(this)),
            "Live foreground service: " + yes(VoiceService.active != null),
            "Modify brightness setting: " + yes(Settings.System.canWrite(this)),
            "Build status: GitHub Debug APK compiled. Phone behavior requires device testing.",
            "Advanced agent / wake word / biometric owner voice: NOT IMPLEMENTED"
        };
        for (String text : checks) { TextView t = new TextView(this); t.setText(text); t.setTextColor(Color.WHITE); t.setTextSize(16); t.setPadding(0, 8, 0, 8); col.addView(t); }
        add(col, "REFRESH", () -> render());
        add(col, "APP PERMISSIONS", () -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + getPackageName()))));
        add(col, "ACCESSIBILITY", () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        add(col, "NOTIFICATION ACCESS", () -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        setContentView(scroll);
    }
    private String yes(boolean ok) { return ok ? "YES" : "NO"; }
    private void add(LinearLayout col, String label, Runnable run) { Button b = new Button(this); b.setText(label); b.setOnClickListener(v -> run.run()); col.addView(b); }
}
