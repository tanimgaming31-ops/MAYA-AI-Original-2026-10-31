package com.tanimworld.maya;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Display;
import java.io.OutputStream;

/** Saves an explicit user-requested Android accessibility screenshot (API 30+). */
public final class ScreenshotHelper {
    private ScreenshotHelper() {}
    public static String capture() {
        MayaAccessibilityService service = MayaAccessibilityService.active;
        if (service == null) return "Enable MAYA Accessibility in Android Settings before taking a screenshot.";
        if (Build.VERSION.SDK_INT < 30) return "Accessibility screenshot requires Android 11+. ScreenCapture consent workflow not included.";
        try {
            service.takeScreenshot(Display.DEFAULT_DISPLAY, service.getMainExecutor(), new AccessibilityService.TakeScreenshotCallback() {
                @Override public void onSuccess(AccessibilityService.ScreenshotResult result) {
                    Bitmap bitmap = null;
                    try {
                        android.hardware.HardwareBuffer buffer = result.getHardwareBuffer();
                        bitmap = Bitmap.wrapHardwareBuffer(buffer, result.getColorSpace());
                        if (bitmap == null) throw new IllegalStateException("Bitmap conversion failed");
                        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        if (copy == null) throw new IllegalStateException("Software bitmap conversion failed");
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "MAYA_" + System.currentTimeMillis() + ".png");
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MAYA");
                        android.net.Uri uri = service.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) throw new IllegalStateException("Gallery unavailable");
                        try (OutputStream stream = service.getContentResolver().openOutputStream(uri)) {
                            if (stream == null || !copy.compress(Bitmap.CompressFormat.PNG, 100, stream)) throw new IllegalStateException("PNG save failed");
                        }
                        copy.recycle();
                        MayaBus.emit("LISTENING", "Screenshot saved in Pictures/MAYA.");
                    } catch (Exception e) { MayaBus.emit("ERROR", "Screenshot could not be saved."); }
                    finally { try { result.getHardwareBuffer().close(); } catch (Exception ignored) {} }
                }
                @Override public void onFailure(int errorCode) { MayaBus.emit("ERROR", "Android screenshot rejected (" + errorCode + ")."); }
            });
            return "Android screenshot requested. Check the result message before assuming it saved.";
        } catch (Exception e) { return "Screenshot unavailable on this device."; }
    }
}
