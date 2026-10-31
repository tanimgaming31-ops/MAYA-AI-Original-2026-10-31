package com.tanimworld.maya;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Foreground-only user-initiated Live audio session, not an always-on wake-word service. */
public final class VoiceService extends Service implements LiveSession.Listener {
    public static final String START = "com.tanimworld.maya.START";
    public static final String STOP = "com.tanimworld.maya.STOP";
    public static volatile VoiceService active;
    private volatile boolean running;
    private LiveSession live;
    private AudioRecord mic;
    private AudioTrack speaker;
    private AcousticEchoCanceler aec;
    private Thread micThread, speakerThread;
    private BlockingQueue<byte[]> speakerQueue;

    @Override public void onCreate() { super.onCreate(); active = this; }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && STOP.equals(intent.getAction())) { stopSelf(); return START_NOT_STICKY; }
        if (running) return START_NOT_STICKY;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            MayaBus.emit("ERROR", "Microphone permission required."); stopSelf(); return START_NOT_STICKY;
        }
        String key = SecretStore.getApiKey(this);
        if (key.isEmpty()) { MayaBus.emit("ERROR", "Enter your Gemini key in Settings first."); stopSelf(); return START_NOT_STICKY; }
        startPersistentNotification();
        running = true;
        SharedPreferences p = getSharedPreferences("maya_options", MODE_PRIVATE);
        speakerQueue = new ArrayBlockingQueue<>(48);
        live = new LiveSession(this, this);
        startPlayback();
        live.connect(key, p.getString("model", "gemini-3.8-live"), p.getString("voice", "Aoede"), p.getString("language", "Auto"));
        MayaBus.emit("CONNECTING", "Connecting to Gemini Live...");
        return START_NOT_STICKY;
    }
    private void startPersistentNotification() {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("maya_live", "MAYA microphone session", NotificationManager.IMPORTANCE_LOW));
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent openIntent = PendingIntent.getActivity(this, 1, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stop = new Intent(this, VoiceService.class).setAction(STOP);
        PendingIntent stopIntent = PendingIntent.getService(this, 2, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this, "maya_live")
                .setContentTitle("MAYA Voice • Microphone active")
                .setContentText("TaNiM world • Tap to open or stop from notification")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentIntent(openIntent)
                .addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stopIntent).build())
                .setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(100, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE |
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else startForeground(100, notification);
    }
    @Override public void onReady() { MayaBus.emit("LISTENING", "Gemini Live connected. Speak to MAYA."); startMic(); }
    private void startMic() {
        int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) { MayaBus.emit("ERROR", "Microphone 16kHz audio unavailable."); return; }
        try {
            mic = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(8192, min * 2));
            if (mic.getState() != AudioRecord.STATE_INITIALIZED) { MayaBus.emit("ERROR", "AudioRecord could not initialize."); return; }
            if (AcousticEchoCanceler.isAvailable()) {
                aec = AcousticEchoCanceler.create(mic.getAudioSessionId()); if (aec != null) aec.setEnabled(true);
            }
            mic.startRecording();
            micThread = new Thread(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                byte[] buf = new byte[1280];
                while (running && mic != null) {
                    int n = mic.read(buf, 0, buf.length);
                    if (n > 0 && live != null) live.sendAudio(buf, n);
                    else if (n < 0) break;
                }
            }, "MayaMic"); micThread.start();
        } catch (SecurityException e) { MayaBus.emit("ERROR", "Microphone permission denied by Android."); }
        catch (Exception e) { MayaBus.emit("ERROR", "Microphone initialization failed."); }
    }
    private void startPlayback() {
        int min = AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) { MayaBus.emit("ERROR", "24kHz audio output not supported."); return; }
        speaker = new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
                new AudioFormat.Builder().setSampleRate(24000).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                Math.max(8192, min * 2), AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        if (speaker.getState() != AudioTrack.STATE_INITIALIZED) { MayaBus.emit("ERROR", "Speaker initialization failed."); return; }
        speaker.play();
        speakerThread = new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            while (running) {
                try {
                    byte[] bytes = speakerQueue.take();
                    if (speaker != null && bytes.length > 0) speaker.write(bytes, 0, bytes.length);
                } catch (InterruptedException e) { break; } catch (Exception e) { break; }
            }
        }, "MayaSpeaker"); speakerThread.start();
    }
    @Override public void onAudio(byte[] pcm) {
        if (pcm != null && pcm.length > 0 && speakerQueue != null) {
            if (!speakerQueue.offer(pcm)) { speakerQueue.poll(); speakerQueue.offer(pcm); }
            MayaBus.emit("SPEAKING", "MAYA speaking...");
        }
    }
    @Override public void onInterrupted() {
        if (speakerQueue != null) speakerQueue.clear();
        try { if (speaker != null) { speaker.pause(); speaker.flush(); speaker.play(); } } catch (Exception ignored) {}
        MayaBus.emit("LISTENING", "Listening again...");
    }
    @Override public void onStatus(String state, String detail) { MayaBus.emit(state, detail); }
    public void sendText(String text) { if (live != null) live.sendText(text); }
    @Override public void onDestroy() {
        running = false;
        if (live != null) { live.disconnect(); live = null; }
        if (micThread != null) micThread.interrupt();
        if (speakerThread != null) speakerThread.interrupt();
        if (aec != null) { aec.release(); aec = null; }
        if (mic != null) { try { mic.stop(); } catch (Exception ignored) {} mic.release(); mic = null; }
        if (speaker != null) { try { speaker.pause(); speaker.flush(); } catch (Exception ignored) {} speaker.release(); speaker = null; }
        active = null; MayaBus.emit("OFFLINE", "Voice session stopped.");
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
