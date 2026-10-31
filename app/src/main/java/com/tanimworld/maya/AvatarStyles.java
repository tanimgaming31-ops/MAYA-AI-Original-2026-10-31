package com.tanimworld.maya;

import android.content.Context;
import java.util.Locale;

/** Seventeen original 2D vector avatar shapes, not copied Playlist assets. */
public final class AvatarStyles {
    public static final String[] NAMES = {
        "orb", "circle", "cat", "star", "flower", "hexagon", "drop", "ghost",
        "droid", "mech", "cloud", "alien", "square", "triangle", "pill", "blob", "heart"
    };
    private AvatarStyles() {}
    public static int selected(Context c) {
        return Math.max(0, Math.min(NAMES.length-1, c.getSharedPreferences("maya_options",0).getInt("avatar_index",0)));
    }
    public static String set(Context c, String name) {
        if (name == null) return "Avatar name is missing.";
        String target=name.trim().toLowerCase(Locale.ROOT);
        for (int i=0;i<NAMES.length;i++) if (NAMES[i].equals(target)) {
            c.getSharedPreferences("maya_options",0).edit().putInt("avatar_index",i).apply();
            return "MAYA avatar changed to "+NAMES[i]+".";
        }
        return "Available avatars: "+String.join(", ",NAMES)+".";
    }
}
