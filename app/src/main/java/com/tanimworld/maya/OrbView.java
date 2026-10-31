package com.tanimworld.maya;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/** Original procedural canvas visualization (no third-party orb images/code). */
public final class OrbView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String state = "IDLE";
    public OrbView(Context c) { super(c); }
    public void setState(String value) { state = value == null ? "IDLE" : value; invalidate(); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float x = getWidth() / 2f, y = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * .25f;
        float pulse = (float)Math.sin(System.currentTimeMillis() / 360.0) * 8f;
        int color = state.equals("SPEAKING") ? Color.rgb(181,104,255) : state.equals("LISTENING") ? Color.rgb(32,230,216) : Color.rgb(65,125,240);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(x, y, radius * 2.2f,
                new int[]{Color.argb(120, 0,180,230), Color.argb(15,0,120,200), Color.TRANSPARENT}, null, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, radius * 2.2f + pulse, paint); paint.setShader(null);
        paint.setShader(new RadialGradient(x - radius * .3f, y - radius * .3f, radius * 1.5f,
                new int[]{Color.WHITE, color, Color.rgb(5,20,60)}, null, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, radius + pulse / 2f, paint); paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(3f); paint.setColor(Color.argb(135, 180,235,255));
        c.drawCircle(x, y, radius * 1.36f + pulse, paint);
        c.drawArc(x - radius*1.65f, y - radius*1.65f, x + radius*1.65f, y + radius*1.65f,
                System.currentTimeMillis() / 22f % 360, 95, false, paint);
        paint.setStyle(Paint.Style.FILL);
        postInvalidateDelayed(45);
    }
}
