package com.tanimworld.maya;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Build;
import android.os.Bundle;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/** Original, deliberately bounded Android command dispatcher. */
public final class CommandRouter {
    private CommandRouter() {}
    public static String runText(Context context, String raw) {
        if (raw == null) return "No command.";
        String text = raw.trim(), lower = text.toLowerCase(Locale.ROOT);
        if (text.isEmpty()) return "Type a command.";
        if (lower.startsWith("remember ")) return remember(context, text.substring(9));
        if (lower.startsWith("forget ")) return forget(context, text.substring(7));
        if (lower.equals("my memory") || lower.equals("what do you remember")) return recall(context, "");
        if (lower.startsWith("recall ")) return recall(context, text.substring(7));
        if (lower.equals("torch on") || lower.equals("flashlight on")) return torch(context, true);
        if (lower.equals("torch off") || lower.equals("flashlight off")) return torch(context, false);
        if (lower.startsWith("open ")) return openApp(context, text.substring(5));
        if (lower.startsWith("search ")) return searchWeb(context, text.substring(7));
        if (lower.startsWith("youtube ")) return youtube(context, text.substring(8));
        if (lower.equals("back") || lower.equals("go back")) return navigation("back");
        if (lower.equals("home") || lower.equals("go home")) return navigation("home");
        if (lower.equals("scroll down")) return navigation("down");
        if (lower.equals("scroll up")) return navigation("up");
        if (lower.startsWith("tap ")) return clickScreen(text.substring(4));
        if (lower.startsWith("type ")) return typeScreen(text.substring(5));
        if (lower.startsWith("avatar ")) return AvatarStyles.set(context,text.substring(7));
        if (lower.equals("avatars")) { context.startActivity(new Intent(context,AvatarPickerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return "Avatar picker opened."; }
        if (lower.equals("choose contact")) { context.startActivity(new Intent(context,ContactPickerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return "Contact picker opened."; }
        if (lower.equals("screenshot vision") || lower.equals("image vision")) {
            context.startActivity(new Intent(context,ImageVisionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return "Image picker opened. You must approve any upload.";
        }
        if (lower.equals("notification replies")) { context.startActivity(new Intent(context,ReplyActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return "Approved reply screen opened."; }
        if (lower.equals("play pause") || lower.equals("pause music")) return media(context,"toggle");
        if (lower.equals("next song")) return media(context,"next");
        if (lower.equals("previous song")) return media(context,"previous");
        if (lower.equals("read screen") || lower.equals("inspect screen")) return inspectScreen();
        if (lower.equals("take screenshot") || lower.equals("screenshot")) return ScreenshotHelper.capture();
        if (lower.startsWith("remind in ")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^remind in (\\d+) minutes? (.+)$").matcher(lower);
            if (m.find()) return ReminderReceiver.schedule(context, Integer.parseInt(m.group(1)), text.substring(m.start(2)));
            return "Try: remind in 10 minutes drink water";
        }
        if (lower.startsWith("volume ")) return volume(context, parsePercent(text.substring(7)));
        if (lower.startsWith("brightness ")) return brightness(context, parsePercent(text.substring(11)));
        return "Not a local command. Start Gemini Live to chat or ask MAYA to perform an available tool action.";
    }
    private static int parsePercent(String v) {
        try { return Math.max(0, Math.min(100, Integer.parseInt(v.replace("%", "").trim()))); }
        catch (NumberFormatException e) { return -1; }
    }
    public static String remember(Context c, String text) {
        if (text == null || text.trim().isEmpty()) return "Please give me something to remember.";
        try (MemoryStore db = new MemoryStore(c)) { db.remember(text); return "Saved locally: " + text; }
    }
    public static String forget(Context c, String text) {
        try (MemoryStore db = new MemoryStore(c)) { return "Removed " + db.forget(text) + " matching memories."; }
    }
    public static String recall(Context c, String q) {
        try (MemoryStore db = new MemoryStore(c)) {
            List<String> entries = db.list(q);
            return entries.isEmpty() ? "No matching memories saved." : String.join("; ", entries);
        }
    }
    public static String torch(Context c, boolean on) {
        try {
            CameraManager manager = (CameraManager)c.getSystemService(Context.CAMERA_SERVICE);
            for (String id : manager.getCameraIdList()) {
                Boolean flash = manager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(flash)) { manager.setTorchMode(id, on); return "Flashlight " + (on ? "on" : "off"); }
            }
            return "This device has no available flashlight.";
        } catch (Exception e) { return "Flashlight unavailable: " + e.getClass().getSimpleName(); }
    }
    public static String volume(Context c, int percent) {
        if (percent < 0) return "Volume requires 0–100.";
        try {
            AudioManager mgr = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
            int max = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mgr.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(max * percent / 100f), 0);
            return "Media volume set to " + percent + "%";
        } catch (Exception e) { return "Volume control unavailable."; }
    }
    public static String brightness(Context c, int percent) {
        if (percent < 0) return "Brightness requires 0–100.";
        if (!Settings.System.canWrite(c)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + c.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(i);
            return "Grant Modify system settings, then retry brightness.";
        }
        try {
            Settings.System.putInt(c.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            boolean ok = Settings.System.putInt(c.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, Math.max(1, Math.round(percent * 255 / 100f)));
            return ok ? "Brightness set to " + percent + "%" : "Brightness change was rejected.";
        } catch (Exception e) { return "Brightness control failed."; }
    }
    public static String openApp(Context c, String target) {
        if (target == null || target.trim().isEmpty()) return "Missing app name.";
        String q = target.trim().toLowerCase(Locale.ROOT);
        try {
            if (q.equals("camera")) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i); return "Camera opened.";
            }
            if (q.equals("settings")) {
                Intent i = new Intent(Settings.ACTION_SETTINGS); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(i); return "Settings opened.";
            }
            if (q.startsWith("http://") || q.startsWith("https://") || q.contains(".com")) return openUrl(c, q);
            PackageManager pm = c.getPackageManager();
            Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> apps = pm.queryIntentActivities(launcher, 0);
            ResolveInfo matched = null;
            for (ResolveInfo info : apps) {
                String label = info.loadLabel(pm).toString().trim().toLowerCase(Locale.ROOT);
                if (label.equals(q)) { matched = info; break; }
            }
            if (matched == null) for (ResolveInfo info : apps) {
                String label = info.loadLabel(pm).toString().toLowerCase(Locale.ROOT);
                if (label.contains(q)) { matched = info; break; }
            }
            if (matched == null) return "App not found: " + target + ". Check installation or app visibility.";
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setClassName(matched.activityInfo.packageName, matched.activityInfo.name);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i); return "Opened " + matched.loadLabel(pm);
        } catch (Exception e) { return "Unable to launch: " + target; }
    }
    public static String openUrl(Context c, String address) {
        try {
            String url = address.startsWith("http://") || address.startsWith("https://") ? address : "https://" + address;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i); return "Opened website.";
        } catch (Exception e) { return "No browser available."; }
    }
    public static String searchWeb(Context c, String q) {
        try { return openUrl(c, "https://www.google.com/search?q=" + URLEncoder.encode(q, "UTF-8")); }
        catch (Exception e) { return "Search unavailable."; }
    }
    public static String youtube(Context c, String q) {
        try { return openUrl(c, "https://www.youtube.com/results?search_query=" + URLEncoder.encode(q, "UTF-8")); }
        catch (Exception e) { return "YouTube search unavailable."; }
    }
    /** Opens a WhatsApp draft only; the person must verify recipient and press Send. */
    public static String whatsappDraft(Context c, String phoneNumber, String message) {
        String digits = phoneNumber == null ? "" : phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 8 || digits.length() > 15) return "Provide an international WhatsApp number with country code.";
        try {
            String url = "https://wa.me/" + digits + "?text=" + URLEncoder.encode(message == null ? "" : message, "UTF-8");
            openUrl(c, url); return "WhatsApp draft opened. Check contact and press Send yourself.";
        } catch (Exception e) { return "Could not open WhatsApp draft."; }
    }
    public static String inspectScreen() {
        MayaAccessibilityService s = MayaAccessibilityService.active;
        return s == null ? "Enable MAYA Accessibility manually in Android Settings first." : s.inspect();
    }
    public static String clickScreen(String target) {
        MayaAccessibilityService s = MayaAccessibilityService.active;
        return s != null && s.clickVisibleLabel(target) ? "Tapped matching label; destination not verified." : "Unable to tap target.";
    }
    public static String typeScreen(String text) {
        MayaAccessibilityService s = MayaAccessibilityService.active;
        return s != null && s.typeIntoFocusedField(text) ? "Entered text into focused editable field." : "No safe focused input field.";
    }
    public static String navigation(String kind) {
        MayaAccessibilityService a = MayaAccessibilityService.active;
        if(a==null)return "Enable MAYA Accessibility to navigate.";
        boolean ok;
        switch(kind){
            case "back":ok=a.goBack();break;
            case "home":ok=a.goHome();break;
            case "down":ok=a.scroll(true);break;
            case "up":ok=a.scroll(false);break;
            default:return "Unknown navigation command.";
        }
        return ok?"Navigation requested; destination not verified.":"Navigation failed or no scrollable area.";
    }
    public static String media(Context context,String kind) {
        int key;
        switch(kind){
            case "toggle":key=KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;break;
            case "next":key=KeyEvent.KEYCODE_MEDIA_NEXT;break;
            case "previous":key=KeyEvent.KEYCODE_MEDIA_PREVIOUS;break;
            default:return "Unknown media action.";
        }
        try{
            AudioManager a=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            a.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,key));
            a.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,key));
            return "Media key dispatched; player response not verified.";
        }catch(Exception e){return "Media control unavailable.";}
    }
    public static String runTool(Context c, String name, JSONObject args) {
        switch (name) {
            case "open_app": return openApp(c, args.optString("app_name"));
            case "set_flashlight": return torch(c, args.optBoolean("enabled"));
            case "set_volume": return volume(c, args.optInt("percent", -1));
            case "set_brightness": return brightness(c, args.optInt("percent", -1));
            case "search_web": return searchWeb(c, args.optString("query"));
            case "youtube_search": return youtube(c, args.optString("query"));
            case "remember": return remember(c, args.optString("fact"));
            case "recall": return recall(c, args.optString("query"));
            case "inspect_screen": return inspectScreen();
            case "take_screenshot": return ScreenshotHelper.capture();
            case "set_reminder": return ReminderReceiver.schedule(c, args.optInt("minutes", 0), args.optString("message"));
            case "click_label": return clickScreen(args.optString("label"));
            case "type_focused": return typeScreen(args.optString("text"));
            case "navigate": return navigation(args.optString("direction"));
            case "media_control": return media(c,args.optString("action"));
            case "select_avatar": return AvatarStyles.set(c,args.optString("style"));
            case "whatsapp_draft": return whatsappDraft(c, args.optString("phone_number"), args.optString("message"));
            default: return "Unknown or unavailable tool: " + name;
        }
    }
}
