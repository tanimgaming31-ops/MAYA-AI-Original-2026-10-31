package com.tanimworld.maya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Only restores previously user-scheduled reminders; never starts microphone. */
public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent) {
        if(intent==null)return;
        String a=intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(a)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(a))
            ReminderReceiver.restore(c);
    }
}
