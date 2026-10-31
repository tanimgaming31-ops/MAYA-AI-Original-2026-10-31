package com.tanimworld.maya;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Explicitly approved, bounded UI macros. No unattended send, payment, delete or permission clicks. */
public final class MacroActivity extends Activity {
    private static final String EXAMPLE="open youtube\nwait 1200\ntap Search\ntype relaxing music";
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final AtomicBoolean canceled=new AtomicBoolean();
    private EditText script;
    private TextView result;
    private volatile boolean executing;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff070b1d);
        LinearLayout col=new LinearLayout(this);col.setPadding(22,26,22,25);col.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(col);
        result=new TextView(this);result.setTextColor(Color.WHITE);result.setTextSize(16);
        result.setText("MAYA • Voice/App Macros (limited)\nReview steps and confirm every run. Never use macros for sending, paying, deleting, granting permissions or sharing private data.");
        col.addView(result);
        script=new EditText(this);script.setTextColor(Color.WHITE);script.setHintTextColor(0xffa0a0a0);
        script.setHint("One step per line");script.setMinLines(6);
        script.setText(getSharedPreferences("maya_macros",0).getString("script",EXAMPLE));col.addView(script);
        button(col,"SAVE MACRO LOCALLY",()->{
            getSharedPreferences("maya_macros",0).edit().putString("script",script.getText().toString()).apply();
            result.setText("Macro saved on this phone. No action started.");
        });
        button(col,"REVIEW AND RUN",()->startMacro());
        button(col,"CANCEL RUN",()->{canceled.set(true);result.setText("Stop requested. Current step may finish.");});
        setContentView(scroll);
    }
    private void button(LinearLayout c,String name,Runnable task){Button b=new Button(this);b.setText(name);b.setOnClickListener(v->task.run());c.addView(b);}
    private static List<String> validate(String text)throws Exception{
        String[] lines=text.split("\\r?\\n");
        List<String> steps=new ArrayList<>();
        long totalWait=0;
        for(String line:lines){
            String step=line.trim();if(step.isEmpty())continue;
            String lower=step.toLowerCase(Locale.ROOT);
            boolean safe=lower.startsWith("open ")||lower.startsWith("tap ")||lower.startsWith("type ")
                    ||lower.equals("back")||lower.equals("home")||lower.equals("scroll up")||lower.equals("scroll down")
                    ||lower.startsWith("wait ");
            if(!safe)throw new Exception("Unsupported step: "+step);
            if(lower.startsWith("tap ")){
                String target=lower.substring(4).trim();
                if(target.matches("(?i).*(send|delete|pay|purchase|buy|transfer|allow|permission|install|subscribe|confirm|post|publish).*"))
                    throw new Exception("Restricted target: "+target);
            }
            if(lower.startsWith("wait ")){
                try{
                    int delay=Integer.parseInt(lower.substring(5).trim());
                    if(delay<0||delay>3000)throw new NumberFormatException();
                    totalWait+=delay;
                }catch(NumberFormatException e){throw new Exception("Wait must be 0-3000 milliseconds.");}
            }
            steps.add(step);
        }
        if(steps.isEmpty()||steps.size()>12||totalWait>10000)throw new Exception("Macro needs 1-12 steps, max 10 seconds of waiting.");
        return steps;
    }
    private void startMacro(){
        if(executing){result.setText("Another macro is running.");return;}
        List<String> steps;
        try{steps=validate(script.getText().toString());}
        catch(Exception e){result.setText("Macro rejected: "+e.getMessage());return;}
        new AlertDialog.Builder(this).setTitle("Run these device actions?")
            .setMessage(String.join("\n",steps)+"\n\nOnly proceed when you trust every step. Accessibility permission is needed for taps and typing.")
            .setNegativeButton("CANCEL",null)
            .setPositiveButton("RUN NOW",(d,w)->runSteps(steps)).show();
    }
    private void runSteps(List<String> steps){
        executing=true;canceled.set(false);
        worker.execute(()->{
            StringBuilder log=new StringBuilder();
            for(String step:steps){
                if(canceled.get()){log.append("Canceled.\n");break;}
                String lower=step.toLowerCase(Locale.ROOT);
                String status;
                try{
                    if(lower.startsWith("wait ")){
                        Thread.sleep(Integer.parseInt(lower.substring(5).trim()));
                        status="Wait completed";
                    }else if(lower.startsWith("open ")){
                        status=CommandRouter.openApp(this,step.substring(5));
                    }else if(lower.startsWith("tap ")){
                        status=CommandRouter.clickScreen(step.substring(4));
                        if(!status.startsWith("Tapped")){log.append("Stopped: ").append(status).append("\n");break;}
                    }else if(lower.startsWith("type ")){
                        status=CommandRouter.typeScreen(step.substring(5));
                        if(!status.startsWith("Entered")){log.append("Stopped: ").append(status).append("\n");break;}
                    }else {
                        status=CommandRouter.navigation(lower.replace("scroll ",""));
                        if(!status.startsWith("Navigation requested")){log.append("Stopped: ").append(status).append("\n");break;}
                    }
                    log.append(step).append(" → ").append(status).append("\n");
                    String snapshot=log.toString();runOnUiThread(()->result.setText(snapshot));
                }catch(Exception error){log.append("Stopped: ").append(error.getMessage());break;}
            }
            executing=false;String summary=log.toString()+"\nMacro ended. Inspect the target app to confirm outcomes.";
            runOnUiThread(()->result.setText(summary));
        });
    }
    @Override protected void onDestroy(){canceled.set(true);worker.shutdownNow();super.onDestroy();}
}
