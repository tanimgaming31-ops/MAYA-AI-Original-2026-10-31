package com.tanimworld.maya;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Uses user-chosen contact, without reading the address book in the background. */
public final class ContactPickerActivity extends Activity {
    private static final int PICK = 31;
    private TextView info;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(26, 45, 26, 26);
        root.setBackgroundColor(0xff070b1d);
        info = new TextView(this);
        info.setTextColor(Color.WHITE);
        info.setTextSize(17);
        info.setText("MAYA Contact Actions\n\nChoose a phone number using Android's contact picker. Dialer will open, but MAYA will not place the call.");
        root.addView(info);
        Button pick = new Button(this);
        pick.setText("CHOOSE CONTACT AND OPEN DIALER");
        pick.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(i, PICK);
        });
        root.addView(pick);
        setContentView(root);
    }
    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req, result, data);
        if (req != PICK || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri selected = data.getData();
        try (Cursor cursor = getContentResolver().query(selected, new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) { info.setText("No phone number found."); return; }
            String name = cursor.getString(0), phone = cursor.getString(1);
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(i);
            info.setText("Opened dialer for " + name + ". Review and press Call yourself.");
        } catch (Exception e) { info.setText("Could not access the selected phone number."); }
    }
}
