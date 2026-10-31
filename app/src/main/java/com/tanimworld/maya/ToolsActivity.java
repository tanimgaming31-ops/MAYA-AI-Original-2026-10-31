package com.tanimworld.maya;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** All existing MAYA tools moved to an accessible, scrollable screen; none removed. */
public final class ToolsActivity extends Activity {
    private TextView result;

    @Override public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getWindow().setStatusBarColor(MayaUi.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(MayaUi.BG);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(MayaUi.dp(this,18),MayaUi.dp(this,19),MayaUi.dp(this,18),MayaUi.dp(this,10));
        TextView back=MayaUi.tap(this,"‹",26,MayaUi.INK,MayaUi.WHITE,this::finish);
        top.addView(back,new LinearLayout.LayoutParams(MayaUi.dp(this,51),MayaUi.dp(this,49)));
        TextView title=MayaUi.center(this,"MAYA TOOLS",24,MayaUi.CORAL,true);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView settings=MayaUi.tap(this,"⚙",23,MayaUi.INK,MayaUi.WHITE,()->open(SettingsActivity.class));
        top.addView(settings,new LinearLayout.LayoutParams(MayaUi.dp(this,51),MayaUi.dp(this,49)));
        page.addView(top);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);column.setPadding(MayaUi.dp(this,18),MayaUi.dp(this,8),MayaUi.dp(this,18),MayaUi.dp(this,25));
        scroll.addView(column);
        result=MayaUi.text(this,"Your existing tools are here. Some actions require Android permissions or confirmation.",14,MayaUi.MUTED,false);
        result.setPadding(MayaUi.dp(this,5),MayaUi.dp(this,9),MayaUi.dp(this,5),MayaUi.dp(this,14));
        column.addView(result);
        section(column,"VOICE & CONVERSATION");
        pair(column,"🎤 Start Voice",this::startVoice,"■ Stop Voice",this::stopVoice);
        pair(column,"☷ Chat",()->open(ChatActivity.class),"◈ Memory",()->reply(CommandRouter.recall(this,"")));
        section(column,"PHONE CONTROLS");
        pair(column,"☀ Torch ON",()->torch(true),"☾ Torch OFF",()->torch(false));
        pair(column,"▣ Camera",()->reply(CommandRouter.openApp(this,"camera")),"⌕ Read Screen",()->reply(CommandRouter.inspectScreen()));
        pair(column,"▤ Screenshot",()->reply(ScreenshotHelper.capture()),"◎ Screen Vision",()->open(ImageVisionActivity.class));
        pair(column,"↩ Back",()->reply(CommandRouter.navigation("back")),"⌂ Home",()->reply(CommandRouter.navigation("home")));
        pair(column,"↓ Scroll Down",()->reply(CommandRouter.navigation("down")),"↑ Scroll Up",()->reply(CommandRouter.navigation("up")));
        pair(column,"▶ Play / Pause",()->reply(CommandRouter.media(this,"toggle")),"» Next Track",()->reply(CommandRouter.media(this,"next")));
        section(column,"MEMORY, MESSAGES & CONTACTS");
        pair(column,"◈ Memory Pack",()->open(MemoryPackActivity.class),"✉ Notification Reply",()->open(ReplyActivity.class));
        pair(column,"☎ Pick Contact",()->open(ContactPickerActivity.class),"☷ App Macros",()->open(MacroActivity.class));
        section(column,"VISION & PERSONALIZATION");
        pair(column,"▧ Document OCR",()->open(DocumentToolsActivity.class),"▦ QR / Barcode",()->open(DocumentToolsActivity.class));
        pair(column,"✿ 17 Avatars",()->open(AvatarPickerActivity.class),"♧ Diagnostics",()->open(DiagnosticsActivity.class));
        pair(column,"✦ Floating ON",()->floating(true),"✕ Floating OFF",()->floating(false));
        section(column,"SETTINGS & HELP");
        pair(column,"⚙ Settings",()->open(SettingsActivity.class),"ⓘ About MAYA",()->new AlertDialog.Builder(this)
            .setTitle("MAYA • TaNiM world")
            .setMessage("Original Android AI assistant with local memories, Gemini Live and native tools. Some advanced functions remain limited; see FEATURE_STATUS.md on GitHub.")
            .setPositiveButton("OK",null).show());
        setContentView(page);
    }
    private void section(LinearLayout parent,String title){
        TextView v=MayaUi.text(this,title,13,MayaUi.ACCENT,true);
        v.setLetterSpacing(.065f);v.setPadding(MayaUi.dp(this,5),MayaUi.dp(this,22),0,MayaUi.dp(this,9));parent.addView(v);
    }
    private void pair(LinearLayout container,String l,Runnable left,String r,Runnable right){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(card(l,left),withMargin());
        row.addView(card(r,right),withMargin());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,MayaUi.dp(this,62));
        p.setMargins(0,0,0,MayaUi.dp(this,9));container.addView(row,p);
    }
    private LinearLayout.LayoutParams withMargin(){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);
        p.setMargins(MayaUi.dp(this,3),0,MayaUi.dp(this,3),0);return p;
    }
    private TextView card(String name,Runnable task){
        TextView v=MayaUi.center(this,name,14,MayaUi.INK,true);
        v.setBackground(MayaUi.gradient(this,MayaUi.SOFT,MayaUi.WHITE,19,MayaUi.BORDER));
        v.setPadding(MayaUi.dp(this,7),MayaUi.dp(this,5),MayaUi.dp(this,7),MayaUi.dp(this,5));
        v.setOnClickListener(view->task.run());return v;
    }
    private void open(Class<?> activity){startActivity(new Intent(this,activity));}
    private void reply(String message){result.setText(message==null?"":message);}
    private void torch(boolean enabled){
        if(enabled&&checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},13);return;
        }
        reply(CommandRouter.torch(this,enabled));
    }
    private void startVoice(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},12);return;
        }
        Intent i=new Intent(this,VoiceService.class).setAction(VoiceService.START);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
        reply("Voice connection requested. Check MAYA's status and Gemini key in Settings.");
    }
    private void stopVoice(){
        startService(new Intent(this,VoiceService.class).setAction(VoiceService.STOP));
        reply("Voice stop requested.");
    }
    private void floating(boolean on){
        if(on&&!Settings.canDrawOverlays(this)){
            reply("Android overlay permission required. Return and tap Floating ON after granting it.");
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:"+getPackageName())));
            return;
        }
        startService(new Intent(this,FloatingAvatarService.class)
            .setAction(on?FloatingAvatarService.START:FloatingAvatarService.STOP));
        reply(on?"Floating MAYA requested.":"Floating MAYA stop requested.");
    }
    @Override public void onRequestPermissionsResult(int code,String[] perms,int[] results){
        super.onRequestPermissionsResult(code,perms,results);
        if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED){
            if(code==12)startVoice(); else if(code==13)torch(true);
        }else reply("Android permission was not granted.");
    }
}
