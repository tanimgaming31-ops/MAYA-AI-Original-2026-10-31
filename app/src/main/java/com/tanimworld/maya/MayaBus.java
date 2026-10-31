package com.tanimworld.maya;

import android.os.Handler;
import android.os.Looper;

/** One small, app-local event channel; never forwards private audio or API keys. */
public final class MayaBus {
    public interface Listener { void onEvent(String state, String detail); }
    private static volatile Listener listener;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private MayaBus() {}
    public static void listen(Listener l) { listener = l; }
    public static void emit(String state, String detail) {
        MAIN.post(() -> { Listener l = listener; if (l != null) l.onEvent(state, detail); });
    }
}
