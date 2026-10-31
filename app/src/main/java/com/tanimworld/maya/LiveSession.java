package com.tanimworld.maya;

import android.content.Context;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/** Independent thin implementation of Google's published Gemini Live JSON protocol. */
public final class LiveSession {
    public interface Listener {
        void onReady();
        void onAudio(byte[] pcm);
        void onInterrupted();
        void onStatus(String state, String detail);
    }
    private final Context context;
    private final Listener events;
    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(20, TimeUnit.SECONDS).pingInterval(30, TimeUnit.SECONDS).build();
    private WebSocket socket;
    private volatile boolean ready;
    public LiveSession(Context context, Listener events) { this.context = context.getApplicationContext(); this.events = events; }

    public void connect(String apiKey, String model, String voice, String language) {
        if (apiKey == null || apiKey.trim().isEmpty()) { events.onStatus("ERROR", "Gemini API key is missing. Open Settings."); return; }
        String endpoint = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent";
        try {
            Request request = new Request.Builder().url(endpoint + "?key=" + URLEncoder.encode(apiKey, "UTF-8")).build();
            socket = client.newWebSocket(request, new WebSocketListener() {
                @Override public void onOpen(WebSocket webSocket, Response response) {
                    try { webSocket.send(makeSetup(model, voice, language).toString()); events.onStatus("CONNECTING", "Gemini setup sent."); }
                    catch (Exception e) { events.onStatus("ERROR", "Unable to build Gemini setup: " + e.getMessage()); webSocket.close(1000, "Setup failure"); }
                }
                @Override public void onMessage(WebSocket webSocket, String text) { receive(text); }
                @Override public void onFailure(WebSocket webSocket, Throwable error, Response response) {
                    ready = false;
                    int code = response == null ? 0 : response.code();
                    events.onStatus("ERROR", "Live connection failed" + (code != 0 ? " (HTTP " + code + ")" : "") + ". Verify API key, model and internet.");
                }
                @Override public void onClosed(WebSocket webSocket, int code, String reason) {
                    ready = false; events.onStatus("OFFLINE", "Gemini Live connection closed.");
                }
            });
        } catch (Exception e) { events.onStatus("ERROR", "Invalid Live endpoint or credentials."); }
    }
    private static JSONObject makeSetup(String model, String voice, String language) throws Exception {
        JSONObject prebuilt = new JSONObject().put("voiceName", voice == null || voice.trim().isEmpty() ? "Aoede" : voice);
        JSONObject speechConfig = new JSONObject().put("voiceConfig", new JSONObject().put("prebuiltVoiceConfig", prebuilt));
        JSONObject generation = new JSONObject().put("responseModalities", new JSONArray().put("AUDIO"))
                .put("speechConfig", speechConfig);
        String systemText = "You are MAYA, an original warm and capable AI assistant developed for TaNiM world. " +
                "Use natural speech and be truthful about tool outcomes. Answer in " +
                (language == null || language.equalsIgnoreCase("Auto") ? "the language the user speaks" : language) +
                ". Never claim an action succeeded unless its tool response confirms it. " +
                "Only perform device actions requested by the user; do not auto-send messages, buy, or install apps.";
        JSONObject setup = new JSONObject()
                .put("model", "models/" + (model == null || model.trim().isEmpty() ? "gemini-3.8-live" : model.replace("models/", "")))
                .put("generationConfig", generation)
                .put("inputAudioTranscription", new JSONObject())
                .put("outputAudioTranscription", new JSONObject())
                .put("systemInstruction", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", systemText))))
                .put("tools", new JSONArray().put(new JSONObject().put("functionDeclarations", declarations())));
        return new JSONObject().put("setup", setup);
    }
    private static JSONObject tool(String name, String description, JSONObject properties, String... required) throws Exception {
        JSONObject schema = new JSONObject().put("type", "OBJECT").put("properties", properties);
        JSONArray req = new JSONArray(); for (String s : required) req.put(s);
        if (required.length != 0) schema.put("required", req);
        return new JSONObject().put("name", name).put("description", description).put("parameters", schema);
    }
    private static JSONObject param(String desc, String type) throws Exception {
        return new JSONObject().put("type", type).put("description", desc);
    }
    private static JSONArray declarations() throws Exception {
        JSONArray funcs = new JSONArray();
        funcs.put(tool("open_app", "Open an installed app named by user", new JSONObject().put("app_name", param("Installed app name", "STRING")), "app_name"));
        funcs.put(tool("set_flashlight", "Switch phone flashlight on/off", new JSONObject().put("enabled", param("True to turn on", "BOOLEAN")), "enabled"));
        funcs.put(tool("set_volume", "Set media volume percentage 0-100", new JSONObject().put("percent", param("0 to 100", "INTEGER")), "percent"));
        funcs.put(tool("set_brightness", "Set screen brightness percentage 0-100; user permission may be required", new JSONObject().put("percent", param("0 to 100", "INTEGER")), "percent"));
        funcs.put(tool("search_web", "Search browser for requested topic", new JSONObject().put("query", param("Search terms", "STRING")), "query"));
        funcs.put(tool("youtube_search", "Open YouTube search results, not direct playback", new JSONObject().put("query", param("Search terms", "STRING")), "query"));
        funcs.put(tool("remember", "Store a user-chosen fact in local memory", new JSONObject().put("fact", param("Fact to remember", "STRING")), "fact"));
        funcs.put(tool("recall", "Look up saved local facts", new JSONObject().put("query", param("Optional matching text", "STRING"))));
        funcs.put(tool("take_screenshot", "Capture screen on Android 11+ only if user explicitly requests a screenshot", new JSONObject()));
        funcs.put(tool("set_reminder", "Create a best-effort local notification reminder", new JSONObject().put("minutes", param("Delay in minutes from 1 to 43200", "INTEGER")).put("message", param("Reminder text", "STRING")), "minutes", "message"));
        funcs.put(tool("inspect_screen", "Read visible text from accessibility tree; only when explicitly requested", new JSONObject()));
        funcs.put(tool("click_label", "Click exactly named visible button only if user requested that click", new JSONObject().put("label", param("Exact visible label", "STRING")), "label"));
        funcs.put(tool("type_focused", "Type user-requested text in the currently focused, non-password editable field", new JSONObject().put("text", param("User provided content", "STRING")), "text"));
        funcs.put(tool("whatsapp_draft", "Open a WhatsApp draft only; user must press Send manually", new JSONObject()
                .put("phone_number", param("International digits including country prefix", "STRING"))
                .put("message", param("Draft message", "STRING")), "phone_number", "message"));
        funcs.put(tool("navigate", "Navigate back/home or scroll up/down through user-enabled Accessibility", new JSONObject().put("direction", param("back, home, up, or down", "STRING")), "direction"));
        funcs.put(tool("media_control", "Play/pause or skip tracks on current media app, not a guaranteed playback result", new JSONObject().put("action",param("toggle, next, or previous", "STRING")), "action"));
        funcs.put(tool("select_avatar", "Change Maya to one of 17 original 2D avatars",new JSONObject().put("style",param("orb, circle, cat, star, flower, hexagon, drop, ghost, droid, mech, cloud, alien, square, triangle, pill, blob, heart", "STRING")), "style"));
        return funcs;
    }
    private void receive(String data) {
        try {
            JSONObject envelope = new JSONObject(data);
            if (envelope.has("setupComplete")) { ready = true; events.onReady(); return; }
            if (envelope.has("toolCall")) {
                JSONArray calls = envelope.getJSONObject("toolCall").optJSONArray("functionCalls");
                if (calls != null) for (int i = 0; i < calls.length(); i++) {
                    JSONObject call = calls.getJSONObject(i);
                    String name = call.optString("name");
                    JSONObject args = call.optJSONObject("args");
                    String result = CommandRouter.runTool(context, name, args == null ? new JSONObject() : args);
                    JSONObject reply = new JSONObject().put("name", name)
                            .put("response", new JSONObject().put("result", result));
                    if (call.has("id")) reply.put("id", call.getString("id"));
                    send(new JSONObject().put("toolResponse", new JSONObject()
                            .put("functionResponses", new JSONArray().put(reply))).toString());
                    events.onStatus("LISTENING", result);
                }
            }
            JSONObject server = envelope.optJSONObject("serverContent");
            if (server == null) return;
            if (server.optBoolean("interrupted")) events.onInterrupted();
            JSONObject input = server.optJSONObject("inputTranscription");
            if (input != null && input.has("text")) events.onStatus("LISTENING", "You: " + input.optString("text"));
            JSONObject output = server.optJSONObject("outputTranscription");
            if (output != null && output.has("text")) events.onStatus("SPEAKING", "MAYA: " + output.optString("text"));
            JSONObject modelTurn = server.optJSONObject("modelTurn");
            if (modelTurn != null) {
                JSONArray parts = modelTurn.optJSONArray("parts");
                if (parts != null) for (int i = 0; i < parts.length(); i++) {
                    JSONObject part = parts.getJSONObject(i);
                    JSONObject inline = part.optJSONObject("inlineData");
                    if (inline != null && inline.has("data")) {
                        byte[] pcm = Base64.decode(inline.optString("data"), Base64.DEFAULT);
                        events.onAudio(pcm);
                    }
                }
            }
        } catch (Exception e) { events.onStatus("ERROR", "Live protocol message could not be processed."); }
    }
    private synchronized void send(String text) { WebSocket s = socket; if (s != null) s.send(text); }
    public void sendAudio(byte[] pcm, int size) {
        if (!ready || size < 1) return;
        try {
            byte[] safe = new byte[size]; System.arraycopy(pcm, 0, safe, 0, size);
            JSONObject payload = new JSONObject().put("mimeType", "audio/pcm;rate=16000")
                    .put("data", Base64.encodeToString(safe, Base64.NO_WRAP));
            send(new JSONObject().put("realtimeInput", new JSONObject().put("audio", payload)).toString());
        } catch (Exception ignored) { }
    }
    public void sendText(String text) {
        if (!ready || text == null || text.trim().isEmpty()) { events.onStatus("ERROR", "Start a connected Live session first."); return; }
        try {
            JSONObject part = new JSONObject().put("text", text);
            JSONArray turns = new JSONArray().put(new JSONObject().put("role", "user").put("parts", new JSONArray().put(part)));
            send(new JSONObject().put("clientContent", new JSONObject().put("turns", turns).put("turnComplete", true)).toString());
        } catch (Exception ignored) { }
    }
    public void disconnect() {
        ready = false; WebSocket s = socket; socket = null;
        if (s != null) s.close(1000, "User stopped session");
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
