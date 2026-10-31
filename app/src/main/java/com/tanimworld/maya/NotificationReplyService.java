package com.tanimworld.maya;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.List;

/** Replies only through an explicit user confirmation in ReplyActivity. */
public final class NotificationReplyService extends NotificationListenerService {
    public static volatile NotificationReplyService active;

    public static final class Candidate {
        public final String key, source, title;
        public final Notification.Action action;
        Candidate(String key, String source, String title, Notification.Action action) {
            this.key = key; this.source = source; this.title = title; this.action = action;
        }
    }

    @Override public void onListenerConnected() { super.onListenerConnected(); active = this; }
    @Override public void onListenerDisconnected() { active = null; super.onListenerDisconnected(); }
    @Override public void onDestroy() { active = null; super.onDestroy(); }

    public List<Candidate> replyable() {
        ArrayList<Candidate> found = new ArrayList<>();
        StatusBarNotification[] notifications = getActiveNotifications();
        if (notifications == null) return found;
        for (StatusBarNotification item : notifications) {
            if (found.size() == 30) break;
            Notification n = item.getNotification();
            if (n == null || n.actions == null) continue;
            for (Notification.Action action : n.actions) {
                RemoteInput[] inputs = action.getRemoteInputs();
                if (inputs == null || inputs.length == 0 || action.actionIntent == null) continue;
                CharSequence name = n.extras == null ? null : n.extras.getCharSequence(Notification.EXTRA_TITLE);
                found.add(new Candidate(item.getKey(), item.getPackageName(),
                        name == null ? "Untitled notification" : name.toString(), action));
                break;
            }
        }
        return found;
    }

    public boolean sendApproved(Candidate candidate, String text) {
        if (candidate == null || text == null || text.trim().isEmpty()) return false;
        try {
            boolean stillActive = false;
            for (StatusBarNotification item : getActiveNotifications()) {
                if (candidate.key.equals(item.getKey())) { stillActive = true; break; }
            }
            if (!stillActive) return false;
            android.os.Bundle results = new android.os.Bundle();
            RemoteInput[] inputs = candidate.action.getRemoteInputs();
            if (inputs == null || inputs.length == 0) return false;
            for (RemoteInput input : inputs) results.putCharSequence(input.getResultKey(), text);
            Intent intent = new Intent();
            RemoteInput.addResultsToIntent(inputs, intent, results);
            candidate.action.actionIntent.send(this, 0, intent);
            return true; // Android accepted the action, not verified remote delivery.
        } catch (Exception e) { return false; }
    }
}
