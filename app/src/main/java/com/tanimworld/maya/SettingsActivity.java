package com.tanimworld.maya;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

public final class SettingsActivity extends Activity {
    private EditText keyInput, modelInput, voiceInput, languageInput;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences prefs = getSharedPreferences("maya_options", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(Color.rgb(7, 11, 29));
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(35, 45, 35, 35);
        scroll.addView(layout);
        addLabel(layout, "MAYA • SETTINGS", 28, 0xFF72E6EE);
        addLabel(layout, "Developer: TaNiM world\nIndependent codebase • Gemini API key stays on device", 14, Color.LTGRAY);
        addLabel(layout, "Gemini API key", 17, Color.WHITE);
        keyInput = field(layout, "Paste your Gemini API key", "");
        addLabel(layout, "Gemini Live model (change if the service retires a model)", 15, Color.WHITE);
        modelInput = field(layout, "Model", prefs.getString("model", "gemini-3.8-live"));
        addLabel(layout, "Voice name", 15, Color.WHITE);
        voiceInput = field(layout, "Voice", prefs.getString("voice", "Aoede"));
        addLabel(layout, "Response language (Auto / Bengali / English ...)", 15, Color.WHITE);
        languageInput = field(layout, "Language", prefs.getString("language", "Auto"));
        addButton(layout, "SAVE KEY & OPTIONS", () -> {
            try {
                String newKey = keyInput.getText().toString().trim();
                if (!newKey.isEmpty()) SecretStore.setApiKey(this, newKey);
                prefs.edit().putString("model", modelInput.getText().toString().trim())
                     .putString("voice", voiceInput.getText().toString().trim())
                     .putString("language", languageInput.getText().toString().trim()).apply();
                Toast.makeText(this, "Settings saved securely", Toast.LENGTH_LONG).show();
                finish();
            } catch (Exception e) { Toast.makeText(this, "Unable to save encrypted key", Toast.LENGTH_LONG).show(); }
        });
        addButton(layout, "REMOVE API KEY", () -> {
            try { SecretStore.setApiKey(this, ""); Toast.makeText(this, "API key removed", Toast.LENGTH_SHORT).show(); }
            catch (Exception e) { Toast.makeText(this, "Key removal failed", Toast.LENGTH_SHORT).show(); }
        });
        addButton(layout, "MEMORY PACK BACKUP / MERGE", () -> startActivity(new Intent(this, MemoryPackActivity.class)));
        addButton(layout, "17 ORIGINAL AVATARS", () -> startActivity(new Intent(this, AvatarPickerActivity.class)));
        addButton(layout, "DIAGNOSTICS", () -> startActivity(new Intent(this, DiagnosticsActivity.class)));
        addButton(layout, "APPROVED NOTIFICATION REPLIES", () -> startActivity(new Intent(this, ReplyActivity.class)));
        addButton(layout, "CONTACT PICKER", () -> startActivity(new Intent(this, ContactPickerActivity.class)));
        addButton(layout, "ON-DEVICE DOCUMENT / RECEIPT / QR", () -> startActivity(new Intent(this, DocumentToolsActivity.class)));
        addButton(layout, "APP MACROS (REVIEW EACH RUN)", () -> startActivity(new Intent(this, MacroActivity.class)));
        addButton(layout, "OPEN FLOATING OVERLAY PERMISSION", () -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));
        addButton(layout, "OPEN ACCESSIBILITY SETTINGS", () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addButton(layout, "OPEN APP PERMISSIONS", () -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))));
        addLabel(layout, "Android requires separate user approval for microphone and Accessibility. No hidden monitoring. Voice is sent to Google only when the active session is started.", 13, Color.LTGRAY);
        setContentView(scroll);
    }
    private void addLabel(LinearLayout box, String t, int size, int color) {
        TextView v = new TextView(this); v.setText(t); v.setTextSize(size); v.setTextColor(color);
        v.setPadding(0, 16, 0, 12); box.addView(v);
    }
    private EditText field(LinearLayout box, String hint, String value) {
        EditText v = new EditText(this); v.setSingleLine(true); v.setTextColor(Color.WHITE); v.setHintTextColor(Color.GRAY);
        v.setHint(hint); v.setText(value); box.addView(v, new LinearLayout.LayoutParams(-1, -2)); return v;
    }
    private void addButton(LinearLayout box, String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setOnClickListener(v -> action.run()); box.addView(b);
    }
}
