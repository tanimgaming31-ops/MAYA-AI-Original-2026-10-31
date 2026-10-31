package com.tanimworld.maya;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Animated original vector character/orb gallery; no external artwork. */
public final class OrbView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private String state = "IDLE";
    public OrbView(Context c) { super(c); }
    public void setState(String value) { state = value == null ? "IDLE" : value; invalidate(); }
    private void polygon(Canvas c,float x,float y,float r,int sides,float rotation) {
        Path path=new Path();
        for(int i=0;i<sides;i++){
            double a=2*Math.PI*i/sides+rotation;
            float px=x+(float)Math.cos(a)*r,py=y+(float)Math.sin(a)*r;
            if(i==0)path.moveTo(px,py);else path.lineTo(px,py);
        }path.close();c.drawPath(path,p);
    }
    private void star(Canvas c,float x,float y,float r,int rays) {
        Path path=new Path();
        for(int i=0;i<rays*2;i++){
            double a=-Math.PI/2+Math.PI*i/rays;
            float ri=i%2==0?r:r*.45f,px=x+(float)Math.cos(a)*ri,py=y+(float)Math.sin(a)*ri;
            if(i==0)path.moveTo(px,py);else path.lineTo(px,py);
        }path.close();c.drawPath(path,p);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float x=getWidth()/2f,y=getHeight()/2f,r=Math.min(getWidth(),getHeight())*.24f;
        float tick=System.currentTimeMillis()/1000f;
        float pulse=(float)Math.sin(tick*3f)*7f;
        int color=state.equals("SPEAKING")?0xffb568ff:state.equals("LISTENING")?0xff21e4d8:0xff569dfc;
        p.setShader(new RadialGradient(x,y,r*2.3f,new int[]{0x884fb9ff,0x22377daa,Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*2.3f+pulse,p);p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
        int style=AvatarStyles.selected(getContext());
        float b=r+pulse*.35f;
        switch(style){
            case 0: p.setShader(new RadialGradient(x-b*.35f,y-b*.3f,b*1.55f,new int[]{Color.WHITE,color,0xff06112e},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,b,p);p.setShader(null);break;
            case 1: c.drawCircle(x,y,b,p);break;
            case 2: c.drawCircle(x,y,b*.83f,p);polygon(c,x-b*.65f,y-b*.74f,b*.4f,3,(float)-Math.PI/2);polygon(c,x+b*.65f,y-b*.74f,b*.4f,3,(float)-Math.PI/2);break;
            case 3: star(c,x,y,b,5);break;
            case 4: for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawCircle(x+(float)Math.cos(a)*b*.6f,y+(float)Math.sin(a)*b*.6f,b*.39f,p);}p.setColor(Color.WHITE);c.drawCircle(x,y,b*.3f,p);break;
            case 5: polygon(c,x,y,b,6,(float)Math.PI/6);break;
            case 6: {Path path=new Path();path.moveTo(x,y-b*1.2f);path.cubicTo(x+b*1.5f,y+b*.1f,x+b*.65f,y+b,x,y+b);path.cubicTo(x-b*.65f,y+b,x-b*1.5f,y+b*.1f,x,y-b*1.2f);c.drawPath(path,p);break;}
            case 7: rect.set(x-b,y-b,x+b,y+b*.85f);c.drawRoundRect(rect,b,b,p);for(int i=0;i<4;i++)c.drawCircle(x-b+i*b*2/3,y+b*.77f,b*.25f,p);break;
            case 8: rect.set(x-b*.85f,y-b*.65f,x+b*.85f,y+b*.6f);c.drawRoundRect(rect,b*.22f,b*.22f,p);c.drawCircle(x-b*.4f,y-b*.9f,b*.15f,p);c.drawCircle(x+b*.4f,y-b*.9f,b*.15f,p);break;
            case 9: rect.set(x-b,y-b*.75f,x+b,y+b*.75f);c.drawRoundRect(rect,b*.1f,b*.1f,p);c.drawRect(x-b*1.12f,y-b*.33f,x-b*.78f,y+b*.33f,p);c.drawRect(x+b*.78f,y-b*.33f,x+b*1.12f,y+b*.33f,p);break;
            case 10: for(int i=0;i<4;i++)c.drawCircle(x-b*.75f+i*b*.49f,y+(i%2)*b*.08f,b*.5f,p);break;
            case 11: rect.set(x-b*.78f,y-b*1.16f,x+b*.78f,y+b*1.06f);c.drawOval(rect,p);break;
            case 12: rect.set(x-b,y-b,x+b,y+b);c.drawRoundRect(rect,b*.08f,b*.08f,p);break;
            case 13: polygon(c,x,y,b,3,(float)-Math.PI/2);break;
            case 14: rect.set(x-b*.56f,y-b*1.1f,x+b*.56f,y+b*1.1f);c.drawRoundRect(rect,b*.56f,b*.56f,p);break;
            case 15: {Path path=new Path();for(int i=0;i<=90;i++){double a=i*Math.PI/45;float ri=b*(.83f+.14f*(float)Math.sin(a*5+tick));float px=x+(float)Math.cos(a)*ri,py=y+(float)Math.sin(a)*ri;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();c.drawPath(path,p);break;}
            default: {Path path=new Path();path.moveTo(x,y+b);path.cubicTo(x-b*1.9f,y-b*.1f,x-b*.9f,y-b*1.4f,x,y-b*.47f);path.cubicTo(x+b*.9f,y-b*1.4f,x+b*1.9f,y-b*.1f,x,y+b);c.drawPath(path,p);break;}
        }
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
        p.setColor(0x99b8f5ff);c.drawCircle(x,y,b*1.38f,p);
        c.drawArc(x-b*1.55f,y-b*1.55f,x+b*1.55f,y+b*1.55f,(tick*40)%360,95,false,p);
        p.setStyle(Paint.Style.FILL);
        postInvalidateDelayed(65);
    }
}
