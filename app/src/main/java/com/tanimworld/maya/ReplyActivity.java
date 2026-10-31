package com.tanimworld.maya;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Human-approved quick replies. Never sends a message on voice intent alone. */
public final class ReplyActivity extends Activity {
    private LinearLayout items;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff070b1d);
        items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setPadding(24, 24, 24, 24);
        scroll.addView(items);
        setContentView(scroll);
        showItems();
    }
    private void label(String message) {
        TextView t = new TextView(this);
        t.setTextColor(Color.WHITE); t.setTextSize(16); t.setPadding(0, 12, 0, 12);
        t.setText(message); items.addView(t);
    }
    private void showItems() {
        items.removeAllViews();
        label("MAYA • Approved Notification Replies\nChoose a notification, enter your reply and approve Send.");
        NotificationReplyService service = NotificationReplyService.active;
        if (service == null) {
            label("Notification access is not enabled. Grant MAYA notification access in Android Settings.");
            Button permission = new Button(this);
            permission.setText("OPEN NOTIFICATION ACCESS SETTINGS");
            permission.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
            items.addView(permission);
            return;
        }
        List<NotificationReplyService.Candidate> candidates = service.replyable();
        if (candidates.isEmpty()) { label("No supported quick-reply notifications are available."); return; }
        for (NotificationReplyService.Candidate c : candidates) {
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(c.title + "\n" + c.source);
            b.setOnClickListener(v -> compose(c));
            items.addView(b);
        }
    }
    private void compose(NotificationReplyService.Candidate choice) {
        EditText text = new EditText(this);
        text.setSingleLine(false);
        text.setHint("Type reply");
        new AlertDialog.Builder(this)
            .setTitle("Reply to " + choice.title)
            .setView(text)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("REVIEW", (dialog, which) -> {
                String value = text.getText().toString().trim();
                if (value.isEmpty()) { showItems(); return; }
                new AlertDialog.Builder(this)
                    .setTitle("Send this reply?")
                    .setMessage("To: " + choice.title + "\nApp: " + choice.source + "\n\n" + value)
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("SEND", (d, w) -> {
                        NotificationReplyService s = NotificationReplyService.active;
                        boolean accepted = s != null && s.sendApproved(choice, value);
                        new AlertDialog.Builder(this).setMessage(accepted
                            ? "Android accepted the reply action. Delivery is not guaranteed."
                            : "Reply unavailable or notification expired.").setPositiveButton("OK", (a, b) -> showItems()).show();
                    }).show();
            }).show();
    }
}
