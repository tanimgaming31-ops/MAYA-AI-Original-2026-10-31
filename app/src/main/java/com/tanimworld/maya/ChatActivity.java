package com.tanimworld.maya;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Separate mobile-friendly chat tab; existing text and Gemini Live tools remain available. */
public final class ChatActivity extends Activity implements MayaBus.Listener {
    private TextView history;
    private EditText message;
    private final StringBuilder chat=new StringBuilder();

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(MayaUi.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout page=new LinearLayout(this);
        page.setBackgroundColor(MayaUi.BG);page.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(MayaUi.dp(this,18),MayaUi.dp(this,17),MayaUi.dp(this,18),MayaUi.dp(this,12));
        TextView back=MayaUi.tap(this,"‹",26,MayaUi.INK,MayaUi.WHITE,this::finish);
        top.addView(back,new LinearLayout.LayoutParams(MayaUi.dp(this,53),MayaUi.dp(this,52)));
        TextView title=MayaUi.center(this,"CHAT WITH MAYA",22,MayaUi.CORAL,true);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView tools=MayaUi.tap(this,"☷",24,MayaUi.INK,MayaUi.WHITE,
            ()->startActivity(new Intent(this,ToolsActivity.class)));
        top.addView(tools,new LinearLayout.LayoutParams(MayaUi.dp(this,53),MayaUi.dp(this,52)));
        page.addView(top);
        ScrollView scroller=new ScrollView(this);
        scroller.setFillViewport(true);scroller.setBackground(MayaUi.shape(this,MayaUi.WHITE,25,MayaUi.BORDER,1));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,0,1);
        sp.setMargins(MayaUi.dp(this,17),0,MayaUi.dp(this,17),MayaUi.dp(this,12));
        page.addView(scroller,sp);
        history=MayaUi.text(this,"MAYA  ♥\nType a command below. Local actions work without Gemini; AI text needs an active Gemini Live voice session.\n",16,MayaUi.INK,false);
        history.setPadding(MayaUi.dp(this,16),MayaUi.dp(this,17),MayaUi.dp(this,16),MayaUi.dp(this,17));
        history.setMovementMethod(new ScrollingMovementMethod());
        scroller.addView(history);
        message=new EditText(this);
        message.setTextColor(MayaUi.INK);message.setHintTextColor(MayaUi.MUTED);
        message.setHint("Message MAYA…");message.setTextSize(16);
        message.setMinLines(2);message.setMaxLines(4);
        message.setBackground(MayaUi.shape(this,MayaUi.WHITE,18,MayaUi.BORDER,1));
        message.setPadding(MayaUi.dp(this,14),MayaUi.dp(this,8),MayaUi.dp(this,14),MayaUi.dp(this,8));
        LinearLayout.LayoutParams edit=new LinearLayout.LayoutParams(-1,-2);
        edit.setMargins(MayaUi.dp(this,18),0,MayaUi.dp(this,18),MayaUi.dp(this,7));
        page.addView(message,edit);
        LinearLayout buttons=new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);buttons.setPadding(MayaUi.dp(this,14),0,MayaUi.dp(this,14),MayaUi.dp(this,15));
        TextView local=MayaUi.tap(this,"RUN LOCAL",15,MayaUi.INK,MayaUi.SOFT,()->send(false));
        TextView ai=MayaUi.tap(this,"ASK GEMINI",15,MayaUi.WHITE,MayaUi.CORAL,()->send(true));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,MayaUi.dp(this,55),1);
        lp.setMargins(MayaUi.dp(this,4),0,MayaUi.dp(this,4),0);
        buttons.addView(local,lp);
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,MayaUi.dp(this,55),1);
        ap.setMargins(MayaUi.dp(this,4),0,MayaUi.dp(this,4),0);
        buttons.addView(ai,ap);page.addView(buttons);
        setContentView(page);
    }
    private void send(boolean gemini){
        String text=message.getText().toString().trim();
        if(text.isEmpty())return;
        message.setText("");
        chat.append("\nYOU: ").append(text).append("\n");
        if(gemini){
            if(VoiceService.active!=null){
                VoiceService.active.sendText(text);
                chat.append("MAYA: Message sent to your active Gemini Live session; voice response may follow.\n");
            }else chat.append("MAYA: Start Voice on the Home tab, then try again.\n");
        }else{
            String outcome=CommandRouter.runText(this,text);
            chat.append("MAYA: ").append(outcome).append("\n");
        }
        if(chat.length()>18000)chat.delete(0,chat.length()-14000);
        history.setText(chat.toString());
    }
    @Override public void onEvent(String state,String detail){
        if(history==null||detail==null||detail.isEmpty())return;
        runOnUiThread(()->{
            if(detail.startsWith("MAYA speaking")||detail.startsWith("Listening again"))return;
            chat.append("MAYA ").append(state).append(": ").append(detail).append("\n");
            if(chat.length()>18000)chat.delete(0,chat.length()-14000);
            history.setText(chat.toString());
        });
    }
    @Override protected void onResume(){super.onResume();MayaBus.listen(this);}
    @Override protected void onPause(){MayaBus.listen(null);super.onPause();}
}
