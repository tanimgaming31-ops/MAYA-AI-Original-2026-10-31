package com.tanimworld.maya;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;

/** Original MAYA Home UI: warm, lightweight and independent of Jerry/Playlist sources. */
public final class MainActivity extends Activity implements MayaBus.Listener {
    private static final int MIC_PERMISSION=12, WEATHER_PERMISSION=61;
    private TextView status, transcript;
    private ParticleOrbView orb;
    private WeatherCard weather;
    private boolean visible;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        render();
    }

    private LinearLayout.LayoutParams margins(int width,int height,int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height);
        p.setMargins(0,MayaUi.dp(this,top),0,0);return p;
    }

    private void render(){
        getWindow().setStatusBarColor(MayaUi.BG);
        getWindow().setNavigationBarColor(MayaUi.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(MayaUi.BG);
        ScrollView scroller=new ScrollView(this);
        scroller.setClipToPadding(false);scroller.setFillViewport(true);
        scroller.setVerticalScrollBarEnabled(false);
        page.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout col=new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(MayaUi.dp(this,18),MayaUi.dp(this,12),MayaUi.dp(this,18),MayaUi.dp(this,18));
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        scroller.addView(col);

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView chatTop=MayaUi.tap(this,"☷",27,MayaUi.INK,MayaUi.WHITE,this::openChat);
        header.addView(chatTop,new LinearLayout.LayoutParams(MayaUi.dp(this,57),MayaUi.dp(this,57)));
        LinearLayout headCenter=new LinearLayout(this);headCenter.setGravity(Gravity.CENTER);headCenter.setOrientation(LinearLayout.VERTICAL);
        TextView title=MayaUi.center(this,"MAYA",34,MayaUi.CORAL,true);
        title.setLetterSpacing(.07f);headCenter.addView(title);
        TextView subtitle=MayaUi.center(this,"AI VOICE COMPANION",11,MayaUi.MUTED,true);
        headCenter.addView(subtitle);
        header.addView(headCenter,new LinearLayout.LayoutParams(0,-2,1));
        TextView setting=MayaUi.tap(this,"⚙",28,MayaUi.INK,MayaUi.WHITE,()->startActivity(new Intent(this,SettingsActivity.class)));
        header.addView(setting,new LinearLayout.LayoutParams(MayaUi.dp(this,57),MayaUi.dp(this,57)));
        col.addView(header,new LinearLayout.LayoutParams(-1,-2));
        TextView greeting=MayaUi.center(this,greeting(),16,MayaUi.INK,true);
        col.addView(greeting,margins(-1,-2,8));

        // The particle sphere is drawn directly with Android Canvas, not copied graphics.
        orb=new ParticleOrbView(this);
        float screenDp=getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density;
        int orbHeight=(int)Math.max(260,Math.min(380,screenDp*.41f));
        col.addView(orb,margins(-1,MayaUi.dp(this,orbHeight),2));
        orb.setOnClickListener(v->toggleVoice());

        status=MayaUi.center(this,"●  MAYA OFF · TAP TO START",13,MayaUi.ACCENT,true);
        status.setLetterSpacing(.06f);
        status.setBackground(MayaUi.shape(this,0xfffff5f1,28,MayaUi.BORDER,1));
        status.setPadding(MayaUi.dp(this,21),MayaUi.dp(this,11),MayaUi.dp(this,21),MayaUi.dp(this,11));
        col.addView(status,margins(-2,-2,1));
        status.setOnClickListener(v->toggleVoice());
        transcript=MayaUi.center(this,"Tap the mic or orb to talk to MAYA",13,MayaUi.MUTED,false);
        transcript.setMaxLines(3);
        col.addView(transcript,margins(-1,-2,6));

        weather=new WeatherCard(this,this::requestWeather);
        col.addView(weather,margins(-1,MayaUi.dp(this,82),23));

        LinearLayout shortcuts=new LinearLayout(this);
        shortcuts.setOrientation(LinearLayout.HORIZONTAL);
        shortcuts.setGravity(Gravity.CENTER);
        addQuick(shortcuts,"▣","Screen","Read / capture",this::screenMenu);
        addQuick(shortcuts,"◈","Memories","Recall & backup",this::memoriesMenu);
        addQuick(shortcuts,"◎","Language","Voice & language",()->startActivity(new Intent(this,SettingsActivity.class)));
        col.addView(shortcuts,margins(-1,MayaUi.dp(this,97),17));
        MayaUi.space(col,this,12);

        // Fixed bottom navigation preserves the whole command toolkit behind Tools.
        LinearLayout nav=new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(MayaUi.dp(this,8),MayaUi.dp(this,7),MayaUi.dp(this,8),MayaUi.dp(this,9));
        nav.setBackground(MayaUi.shape(this,MayaUi.WHITE,30,0,0));
        navigation(nav,"⌂","Home",this::showHome);
        navigation(nav,"▤","Chat",this::openChat);
        TextView mic=MayaUi.center(this,"🎙",27,MayaUi.CORAL,true);
        mic.setContentDescription("Start or stop voice session");
        mic.setBackground(MayaUi.gradient(this,0xffffded6,0xffffeaf0,55,MayaUi.CORAL));
        mic.setOnClickListener(v->toggleVoice());
        nav.addView(mic,new LinearLayout.LayoutParams(0,MayaUi.dp(this,70),1.15f));
        navigation(nav,"⚙","Settings",()->startActivity(new Intent(this,SettingsActivity.class)));
        navigation(nav,"☷","Tools",()->startActivity(new Intent(this,ToolsActivity.class)));
        page.addView(nav,new LinearLayout.LayoutParams(-1,MayaUi.dp(this,83)));
        setContentView(page);
    }
    private String greeting(){
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour>=5&&hour<12)return "Good morning! Your MAYA is here ♥";
        if(hour<17)return "Good afternoon! Welcome back ♥";
        if(hour<21)return "Good evening! Welcome back ♥";
        return "Good night! Your MAYA is here ♥";
    }
    private void addQuick(LinearLayout row,String symbol,String name,String description,Runnable action){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(MayaUi.dp(this,10),MayaUi.dp(this,8),MayaUi.dp(this,6),MayaUi.dp(this,8));
        card.setBackground(MayaUi.gradient(this,MayaUi.SOFT,MayaUi.WHITE,21,MayaUi.BORDER));
        TextView icon=MayaUi.text(this,symbol,23,MayaUi.CORAL,true);
        TextView label=MayaUi.text(this,name,13,MayaUi.INK,true);
        TextView hint=MayaUi.text(this,description,10,MayaUi.MUTED,false);
        card.addView(icon);card.addView(label);card.addView(hint);
        card.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);
        p.setMargins(MayaUi.dp(this,3),0,MayaUi.dp(this,3),0);
        row.addView(card,p);
    }
    private void navigation(LinearLayout parent,String icon,String text,Runnable action){
        LinearLayout tab=new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);tab.setGravity(Gravity.CENTER);
        TextView symbol=MayaUi.center(this,icon,25,MayaUi.ACCENT,true);
        TextView name=MayaUi.center(this,text,11,MayaUi.INK,true);
        tab.addView(symbol);tab.addView(name);
        tab.setOnClickListener(v->action.run());
        parent.addView(tab,new LinearLayout.LayoutParams(0,-1,1));
    }
    private void showHome(){ /* Already on the home tab. */ }
    private void openChat(){startActivity(new Intent(this,ChatActivity.class));}
    private void screenMenu(){
        String[] options={"Read visible screen text","Capture screenshot","Analyze selected screenshot with Gemini"};
        new AlertDialog.Builder(this).setTitle("MAYA Screen Tools").setItems(options,(dialog,which)->{
            if(which==0)say(CommandRouter.inspectScreen());
            if(which==1)say(ScreenshotHelper.capture());
            if(which==2)startActivity(new Intent(this,ImageVisionActivity.class));
        }).setNegativeButton("CLOSE",null).show();
    }
    private void memoriesMenu(){
        new AlertDialog.Builder(this).setTitle("MAYA Memories")
            .setItems(new String[]{"Recall saved memories","Import / export Memory Pack"},(d,index)->{
                if(index==0)say(CommandRouter.recall(this,""));
                else startActivity(new Intent(this,MemoryPackActivity.class));
            }).show();
    }
    private void say(String text){ if(transcript!=null)transcript.setText(text==null?"":text); }
    private void toggleVoice(){
        if(VoiceService.active!=null){stopVoice();return;}
        startVoice();
    }
    private void startVoice(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION);return;
        }
        Intent intent=new Intent(this,VoiceService.class).setAction(VoiceService.START);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(intent);else startService(intent);
        if(status!=null)status.setText("●  MAYA CONNECTING…");
    }
    private void stopVoice(){
        startService(new Intent(this,VoiceService.class).setAction(VoiceService.STOP));
        if(status!=null)status.setText("●  MAYA OFF · TAP TO START");
        if(orb!=null)orb.setState("OFFLINE");
    }
    private void requestWeather(){
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},WEATHER_PERMISSION);return;
        }
        try{
            LocationManager mgr=(LocationManager)getSystemService(LOCATION_SERVICE);
            Location loc=null;
            for(String provider:new String[]{LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER}){
                if(mgr.getAllProviders().contains(provider)){
                    Location candidate=mgr.getLastKnownLocation(provider);
                    if(candidate!=null&&(loc==null||candidate.getTime()>loc.getTime()))loc=candidate;
                }
            }
            if(loc!=null){weather.load(loc);return;}
            if(Build.VERSION.SDK_INT>=30&&mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                weather.showError("Locating…");
                mgr.getCurrentLocation(LocationManager.NETWORK_PROVIDER,null,getMainExecutor(),location->{
                    if(weather!=null)weather.load(location);
                });
            }else{
                weather.showError("Enable location and tap again");
            }
        }catch(Exception ex){weather.showError("Location unavailable • Tap to retry");}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] result){
        super.onRequestPermissionsResult(request,permissions,result);
        if(result.length==0)return;
        if(request==MIC_PERMISSION&&result[0]==PackageManager.PERMISSION_GRANTED)startVoice();
        if(request==WEATHER_PERMISSION){
            if(result[0]==PackageManager.PERMISSION_GRANTED)requestWeather();
            else if(weather!=null)weather.showError("Location not allowed");
        }
    }
    @Override public void onEvent(String state,String detail){
        runOnUiThread(()->{
            if(status!=null)status.setText("●  MAYA "+state+" · "+(VoiceService.active!=null?"TAP TO STOP":"TAP TO START"));
            if(orb!=null)orb.setState(state);
            if(detail!=null&&!detail.isEmpty())say(detail);
        });
    }
    @Override protected void onResume(){
        super.onResume();
        visible=true;MayaBus.listen(this);
        if(orb!=null)orb.setAnimationEnabled(true);
        if(status!=null&&VoiceService.active!=null)status.setText("●  MAYA ACTIVE · TAP TO STOP");
    }
    @Override protected void onPause(){
        visible=false;
        if(orb!=null)orb.setAnimationEnabled(false);
        MayaBus.listen(null);super.onPause();
    }
}
