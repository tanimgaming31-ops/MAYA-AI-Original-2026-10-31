package com.tanimworld.maya;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

/** User-controlled floating vector MAYA avatar; no surveillance or auto microphone. */
public final class FloatingAvatarService extends Service {
    public static final String START="com.tanimworld.maya.FLOATING_START";
    public static final String STOP="com.tanimworld.maya.FLOATING_STOP";
    private WindowManager windows;
    private FrameLayout overlay;

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){
            MayaBus.emit("ERROR","Allow Display over other apps in Android Settings.");
            stopSelf();return START_NOT_STICKY;
        }
        if(overlay!=null)return START_NOT_STICKY;
        try{
            windows=(WindowManager)getSystemService(WINDOW_SERVICE);
            overlay=new FrameLayout(this);
            overlay.setBackgroundColor(0x33050920);
            OrbView avatar=new OrbView(this);
            overlay.addView(avatar,new FrameLayout.LayoutParams(-1,-1));
            Button open=new Button(this);
            open.setAllCaps(false);open.setText("MAYA");
            FrameLayout.LayoutParams button=new FrameLayout.LayoutParams(110,60,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
            overlay.addView(open,button);
            open.setOnClickListener(v->{
                Intent i=new Intent(this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
            WindowManager.LayoutParams p=new WindowManager.LayoutParams(185,230,
                Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
            p.gravity=Gravity.END|Gravity.CENTER_VERTICAL;
            windows.addView(overlay,p);
            MayaBus.emit("READY","Floating MAYA avatar started. Hide it from MAYA's main screen.");
            return START_NOT_STICKY;
        }catch(Exception e){
            MayaBus.emit("ERROR","Floating avatar unavailable: "+e.getClass().getSimpleName());
            stopSelf();return START_NOT_STICKY;
        }
    }
    @Override public void onDestroy(){
        if(overlay!=null&&windows!=null){try{windows.removeView(overlay);}catch(Exception ignored){}}
        overlay=null;windows=null;super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}
