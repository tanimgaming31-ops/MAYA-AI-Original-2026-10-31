package com.tanimworld.maya;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/** User-requested local reminder: battery/OS delivery is best-effort, not an exact alarm. */
public final class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "maya_reminders";
    @Override public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL, "MAYA reminders", NotificationManager.IMPORTANCE_HIGH));
        String message = intent == null ? "Your reminder" : intent.getStringExtra("message");
        if (message == null) message = "Your reminder";
        PendingIntent open = PendingIntent.getActivity(context, 7,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(context, CHANNEL)
                .setContentTitle("MAYA Reminder").setContentText(message)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setAutoCancel(true).setContentIntent(open).build();
        try { manager.notify((int)(System.currentTimeMillis() & 0x7fffffff), n); }
        catch (SecurityException e) { MayaBus.emit("ERROR", "Notifications are not permitted on this device."); }
    }
    public static String schedule(Context context, int minutes, String message) {
        if (minutes <= 0 || minutes > 60 * 24 * 30) return "Reminder must be between 1 minute and 30 days.";
        if (message == null || message.trim().isEmpty()) return "Please add reminder text.";
        try {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("message", message.trim());
            int id = (int)(System.currentTimeMillis() & 0x7fffffff);
            PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            long trigger = SystemClock.elapsedRealtime() + minutes * 60_000L;
            alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pi);
            return "Reminder requested in " + minutes + " minutes. Delivery is approximate; Android may delay it.";
        } catch (Exception e) { return "Unable to schedule reminder."; }
    }
}
