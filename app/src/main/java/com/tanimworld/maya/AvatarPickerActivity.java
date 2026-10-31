package com.tanimworld.maya;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public final class AvatarPickerActivity extends Activity {
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(28,38,28,25);
        col.setBackgroundColor(0xff070b1d);
        TextView header = new TextView(this);
        header.setText("MAYA • 17 ORIGINAL AVATARS\nThese are animated 2D designs, not 3D rigged people.");
        header.setTextSize(19); header.setTextColor(Color.WHITE); col.addView(header);
        OrbView preview = new OrbView(this);
        col.addView(preview,new LinearLayout.LayoutParams(-1,360));
        Spinner picker = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, AvatarStyles.NAMES);
        picker.setAdapter(adapter); picker.setSelection(AvatarStyles.selected(this)); col.addView(picker);
        Button apply = new Button(this); apply.setText("SAVE AVATAR");
        apply.setOnClickListener(v -> { AvatarStyles.set(this, AvatarStyles.NAMES[picker.getSelectedItemPosition()]); preview.invalidate(); });
        col.addView(apply);
        setContentView(col);
    }
}
