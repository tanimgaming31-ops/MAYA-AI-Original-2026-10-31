package com.tanimworld.maya;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

/** Independently drawn rotating particle sphere. No Jerry/Playlist artwork or source. */
public final class ParticleOrbView extends View {
    private static final int COUNT = 740;
    private final float[] latitude = new float[COUNT], longitude = new float[COUNT];
    private final Paint dots = new Paint(Paint.ANTI_ALIAS_FLAG), halo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rings = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private String state="OFFLINE";
    private boolean animation=true;

    public ParticleOrbView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentDescription("MAYA animated voice orb. Tap to start or stop voice.");
        setFocusable(true);
        for (int i=0;i<COUNT;i++) {
            float y=1f-2f*(i+.5f)/COUNT;
            latitude[i]=(float)Math.asin(y);
            longitude[i]=(float)(2.399963229728653 * i);
        }
    }
    public void setState(String value) { state=value==null?"OFFLINE":value;invalidate(); }
    public void setAnimationEnabled(boolean enabled){animation=enabled;invalidate();}
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        final float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        float x=w*.5f,y=h*.5f;
        float r=Math.min(w*.38f,h*.375f);
        double time=SystemClock.uptimeMillis()/1000.0;
        float tick=animation?(float)time:0f;
        boolean speaking=state.equalsIgnoreCase("SPEAKING");
        boolean listening=state.equalsIgnoreCase("LISTENING");
        float beat=(float)Math.sin(tick*(speaking?5:2.6f))*(speaking?8:3);
        int pink=speaking?0xffdf67ce:listening?0xffff698e:0xffff5d76;
        int deep=speaking?0xffbc62e0:listening?0xffff7963:0xffd2588e;

        // Volumetric blush halo and rotating orbital arcs.
        halo.setShader(new RadialGradient(x,y,r*1.55f,
            new int[]{0x55f5a1b4,0x24d28ecf,0x00ffffff},new float[]{0f,.65f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*1.55f,halo);halo.setShader(null);
        rings.setStyle(Paint.Style.STROKE);
        rings.setStrokeCap(Paint.Cap.ROUND);
        rings.setStrokeWidth(Math.max(1.1f,r*.012f));
        for(int i=0;i<3;i++){
            float ro=r*(1.22f+i*.06f);
            oval.set(x-ro,y-ro*.94f,x+ro,y+ro*.94f);
            rings.setColor(i==0?0x88c991e1:i==1?0x55ff8499:0x22e7a3d2);
            c.drawArc(oval, tick*(13+i*7)+i*110f, 94+i*24, false,rings);
        }
        rings.setStyle(Paint.Style.FILL);
        for(int i=0;i<23;i++){
            float theta=(float)(i*2.39996+tick*.028);
            float dist=r*(1.23f+(i%7)*.073f);
            float dx=(float)Math.cos(theta)*dist;
            float dy=(float)Math.sin(theta)*dist*.83f;
            rings.setColor(i%3==0?0x88ff7989:i%3==1?0x779d75e7:0x77ffa253);
            c.drawCircle(x+dx,y+dy,Math.max(1f,r*(i%4+1)*.007f),rings);
        }

        // Fibonacci sphere points projected with real front/back depth and perspective.
        float spin=tick*.15f;
        float cs=(float)Math.cos(spin), sn=(float)Math.sin(spin);
        float tilt=.2f, ct=(float)Math.cos(tilt), st=(float)Math.sin(tilt);
        for(int i=0;i<COUNT;i++){
            float cl=(float)Math.cos(latitude[i]);
            float px=cl*(float)Math.cos(longitude[i]);
            float py=(float)Math.sin(latitude[i]);
            float pz=cl*(float)Math.sin(longitude[i]);
            float rx=px*cs+pz*sn;
            float rz=pz*cs-px*sn;
            float ry=py*ct-rz*st, zz=py*st+rz*ct;
            float perspective=1f+zz*.10f;
            float sx=x+rx*(r+beat)*perspective;
            float sy=y+ry*(r+beat)*perspective;
            float alpha=(zz+1f)*.5f;
            float dot=r*(.0065f+.0065f*alpha+(i%7)*.0005f);
            dots.setColor(alpha>.55f?pink:deep);
            dots.setAlpha((int)(95+alpha*154));
            c.drawCircle(sx,sy,dot,dots);
        }
        dots.setAlpha(255);
        // Small central reflective highlight, not a solid blue ball.
        halo.setShader(new RadialGradient(x-r*.18f,y-r*.17f,r*.88f,
            new int[]{0x33ffffff,0x11ffb9cd,0x00ffffff},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*.88f,halo);halo.setShader(null);
        if(animation&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(45);
    }
}
