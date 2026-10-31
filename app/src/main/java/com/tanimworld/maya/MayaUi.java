package com.tanimworld.maya;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/** Original reusable MAYA pastel UI, no third-party art, fonts or layouts. */
public final class MayaUi {
    public static final int BG = 0xfffff0eb;
    public static final int INK = 0xff341d31;
    public static final int MUTED = 0xffa27e95;
    public static final int CORAL = 0xffff684b;
    public static final int ACCENT = 0xffad6f9b;
    public static final int BORDER = 0xffdcc1c1;
    public static final int WHITE = 0xfffffbfa;
    public static final int SOFT = 0xffffe5df;
    public static final int LILAC = 0xffefdde9;

    private MayaUi() {}
    public static int dp(Context ctx, float n) { return (int)(n * ctx.getResources().getDisplayMetrics().density + 0.5f); }
    public static GradientDrawable shape(Context ctx, int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(ctx, radius));
        if(strokeWidth > 0) d.setStroke(dp(ctx, strokeWidth), strokeColor);
        return d;
    }
    public static GradientDrawable gradient(Context ctx, int left, int right, int radius, int border) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{left, right});
        d.setCornerRadius(dp(ctx, radius));
        if(border!=0) d.setStroke(dp(ctx, 1), border);
        return d;
    }
    public static TextView text(Context c, String value, int size, int color, boolean bold) {
        TextView t=new TextView(c); t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        t.setIncludeFontPadding(true);
        return t;
    }
    public static TextView center(Context c,String value,int size,int color,boolean bold){
        TextView t=text(c,value,size,color,bold);t.setGravity(Gravity.CENTER);return t;
    }
    public static TextView tap(Context c,String value,int size,int textColor,int backgroundColor,Runnable action) {
        TextView v=center(c,value,size,textColor,true);
        v.setBackground(shape(c,backgroundColor,22,BORDER,1));
        v.setPadding(dp(c,8),dp(c,10),dp(c,8),dp(c,10));
        v.setOnClickListener(w->action.run());
        v.setClickable(true); v.setFocusable(true);
        return v;
    }
    public static void space(android.widget.LinearLayout parent,Context c,int height){
        View v=new View(c);parent.addView(v,new android.widget.LinearLayout.LayoutParams(1,dp(c,height)));
    }
}
