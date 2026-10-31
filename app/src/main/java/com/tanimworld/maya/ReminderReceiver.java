package com.tanimworld.maya;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/** Persisted, user-created inexact reminders; rescheduled after reboot. */
public final class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL="maya_reminders";
    private static final String PREFS="maya_reminders";
    private static final String KEY="pending";
    private static final Object LOCK=new Object();

    @Override public void onReceive(Context context, Intent intent) {
        int id=intent==null?-1:intent.getIntExtra("reminder_id",-1);
        String message=intent==null?null:intent.getStringExtra("message");
        if(id>=0) remove(context,id);
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL,"MAYA reminders",NotificationManager.IMPORTANCE_HIGH));
        PendingIntent open=PendingIntent.getActivity(context,7,new Intent(context,MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Notification note=new Notification.Builder(context,CHANNEL).setContentTitle("MAYA Reminder")
                .setContentText(message==null?"Your reminder":message)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setAutoCancel(true).setContentIntent(open).build();
        try{ manager.notify(id>=0?id:(int)(System.currentTimeMillis()&0x7fffffff),note); }
        catch(SecurityException e){ MayaBus.emit("ERROR","Notifications not permitted."); }
    }

    private static JSONArray read(Context c) {
        try{return new JSONArray(c.getSharedPreferences(PREFS,0).getString(KEY,"[]"));}
        catch(Exception e){return new JSONArray();}
    }
    private static void write(Context c,JSONArray items) {
        c.getSharedPreferences(PREFS,0).edit().putString(KEY,items.toString()).commit();
    }
    private static void remove(Context c,int id) {
        synchronized(LOCK){
            JSONArray old=read(c), keep=new JSONArray();
            for(int i=0;i<old.length();i++) {
                JSONObject item=old.optJSONObject(i);
                if(item!=null && item.optInt("id",-1)!=id)keep.put(item);
            }
            write(c,keep);
        }
    }
    private static void register(Context c,int id,long at,String text) {
        Intent intent=new Intent(c,ReminderReceiver.class);
        intent.putExtra("reminder_id",id);
        intent.putExtra("message",text);
        PendingIntent pending=PendingIntent.getBroadcast(c,id,intent,
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(alarm==null)throw new IllegalStateException("Alarm service unavailable");
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending);
    }
    public static String schedule(Context c,int minutes,String message) {
        if(minutes<=0 || minutes>43200)return "Reminder delay must be 1 minute to 30 days.";
        if(message==null || message.trim().isEmpty())return "Reminder message is missing.";
        synchronized(LOCK){
            JSONArray existing=read(c);
            if(existing.length()>=100)return "Reminder queue full. Maximum 100 pending.";
            int id=(int)(SystemClock.elapsedRealtime()&0x7fffffff);
            long at=System.currentTimeMillis()+minutes*60000L;
            try{
                register(c,id,at,message.trim());
                existing.put(new JSONObject().put("id",id).put("at",at).put("message",message.trim()));
                write(c,existing);
                return "Reminder saved for approximately "+minutes+" minutes from now. Android may delay it.";
            }catch(Exception e){return "Unable to set reminder: "+e.getClass().getSimpleName();}
        }
    }
    public static void restore(Context c){
        synchronized(LOCK){
            JSONArray old=read(c), keep=new JSONArray();
            long now=System.currentTimeMillis();
            for(int i=0;i<old.length();i++){
                JSONObject item=old.optJSONObject(i);if(item==null)continue;
                try{
                    int id=item.getInt("id");long at=item.getLong("at");
                    String message=item.getString("message");
                    // overdue reminders are delivered shortly after boot (not silently discarded)
                    if(at<now)at=now+30000L;
                    register(c,id,at,message);
                    keep.put(new JSONObject().put("id",id).put("at",at).put("message",message));
                }catch(Exception ignored){}
            }
            write(c,keep);
        }
    }
}
