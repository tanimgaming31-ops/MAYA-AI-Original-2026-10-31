package com.tanimworld.maya;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/** Local, explicit SAF JSON backup/merge. It never exports the Gemini API key. */
public final class MemoryPackActivity extends Activity {
    private static final int EXPORT = 401, IMPORT = 402;
    private TextView result;
    private String pendingJson;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff070b1d);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(28, 35, 28, 28);
        scroll.addView(panel);
        result = new TextView(this);
        result.setTextColor(Color.WHITE);
        result.setTextSize(16);
        result.setText("MAYA Memory Packs\n\nExport or merge your saved facts. Imports never erase existing entries. API keys are never included.");
        panel.addView(result);
        Button export = new Button(this);
        export.setText("EXPORT MEMORY JSON");
        export.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE, "MAYA_Memory_Backup.json");
            startActivityForResult(i, EXPORT);
        });
        panel.addView(export);
        Button importButton = new Button(this);
        importButton.setText("IMPORT AND MERGE MEMORY JSON");
        importButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, IMPORT);
        });
        panel.addView(importButton);
        setContentView(scroll);
    }

    private String backup() throws Exception {
        JSONObject doc = new JSONObject();
        doc.put("format", "maya-memory-pack-v1");
        doc.put("created_at", System.currentTimeMillis());
        JSONArray data = new JSONArray();
        try (MemoryStore db = new MemoryStore(this);
             Cursor c = db.getReadableDatabase().rawQuery("SELECT text,saved_at FROM facts ORDER BY id", null)) {
            while (c.moveToNext()) {
                data.put(new JSONObject().put("text", c.getString(0)).put("saved_at", c.getLong(1)));
            }
        }
        doc.put("facts", data);
        return doc.toString(2);
    }

    private String merge(String json) throws Exception {
        JSONObject doc = new JSONObject(json);
        if (!"maya-memory-pack-v1".equals(doc.optString("format"))) throw new Exception("Unsupported memory pack format.");
        JSONArray items = doc.getJSONArray("facts");
        if (items.length() > 10000) throw new Exception("Memory pack exceeds 10,000 entries.");
        Set<String> distinct = new HashSet<>();
        int added = 0;
        try (MemoryStore store = new MemoryStore(this)) {
            SQLiteDatabase db = store.getWritableDatabase();
            try (Cursor c = db.rawQuery("SELECT text FROM facts", null)) {
                while (c.moveToNext()) distinct.add(c.getString(0));
            }
            db.beginTransaction();
            try {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject entry = items.getJSONObject(i);
                    String fact = entry.getString("text").trim();
                    if (fact.isEmpty() || fact.length() > 4000 || !distinct.add(fact)) continue;
                    ContentValues row = new ContentValues();
                    row.put("text", fact);
                    row.put("saved_at", entry.optLong("saved_at", System.currentTimeMillis()));
                    if (db.insertOrThrow("facts", null, row) != -1) added++;
                }
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
        return "Memory merge completed. New facts: " + added + ". Existing memories were kept.";
    }

    @Override protected void onActivityResult(int request, int code, Intent data) {
        super.onActivityResult(request, code, data);
        if (code != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (request == EXPORT) {
                byte[] bytes = backup().getBytes(StandardCharsets.UTF_8);
                try (OutputStream output = getContentResolver().openOutputStream(uri)) {
                    if (output == null) throw new Exception("Could not open backup file.");
                    output.write(bytes);
                }
                result.setText("Memory backup saved. Keep the JSON file somewhere private.");
            } else if (request == IMPORT) {
                byte[] bytes;
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new Exception("Could not open memory pack.");
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    byte[] part = new byte[8192];
                    int n;
                    while ((n = input.read(part)) != -1) {
                        if (buffer.size() + n > 2_000_000) throw new Exception("Memory backup exceeds 2 MB.");
                        buffer.write(part, 0, n);
                    }
                    bytes = buffer.toByteArray();
                }
                if (bytes.length > 2_000_000) throw new Exception("Memory backup exceeds 2 MB.");
                pendingJson = new String(bytes, StandardCharsets.UTF_8);
                new JSONObject(pendingJson).getJSONArray("facts");
                new AlertDialog.Builder(this)
                    .setTitle("Import this memory pack?")
                    .setMessage("MAYA will merge new facts. Existing facts will not be deleted. Import only files you trust.")
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("MERGE", (d, w) -> {
                        try { result.setText(merge(pendingJson)); }
                        catch (Exception e) { result.setText("Import failed: " + e.getMessage()); }
                        pendingJson = null;
                    }).show();
            }
        } catch (Exception e) { result.setText("Memory operation failed: " + e.getMessage()); }
    }
}
