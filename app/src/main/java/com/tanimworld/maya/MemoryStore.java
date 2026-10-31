package com.tanimworld.maya;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/** Entirely local key/fact memory; no third-party memory server. */
public final class MemoryStore extends SQLiteOpenHelper {
    public MemoryStore(Context c) { super(c, "maya_memory.sqlite", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE facts(id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT NOT NULL, saved_at INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    public synchronized long remember(String fact) {
        if (fact == null || fact.trim().isEmpty()) return -1;
        ContentValues values = new ContentValues(); values.put("text", fact.trim()); values.put("saved_at", System.currentTimeMillis());
        return getWritableDatabase().insert("facts", null, values);
    }
    public synchronized List<String> list(String search) {
        List<String> found = new ArrayList<>(); String pattern = "%" + (search == null ? "" : search.trim()) + "%";
        try (Cursor cursor = getReadableDatabase().query("facts", new String[]{"text"}, "text LIKE ?", new String[]{pattern}, null, null, "saved_at DESC", "50")) {
            while (cursor.moveToNext()) found.add(cursor.getString(0));
        }
        return found;
    }
    public synchronized int forget(String search) {
        if (search == null || search.trim().isEmpty()) return 0;
        return getWritableDatabase().delete("facts", "text LIKE ?", new String[]{"%" + search.trim() + "%"});
    }
}
