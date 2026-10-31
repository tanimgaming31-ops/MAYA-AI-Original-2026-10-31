package com.tanimworld.maya;

import android.content.Context;
import android.location.Location;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Optional local weather; no fake default city or simulated readings. */
public final class WeatherCard extends LinearLayout {
    private final TextView temperature, conditions, icon;
    private final ExecutorService network=Executors.newSingleThreadExecutor();
    private final Handler ui=new Handler(Looper.getMainLooper());
    private boolean loading=false;

    public WeatherCard(Context c,Runnable requestLocation) {
        super(c);
        setOrientation(HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);
        setPadding(MayaUi.dp(c,18),MayaUi.dp(c,15),MayaUi.dp(c,17),MayaUi.dp(c,15));
        setBackground(MayaUi.gradient(c,0xffffe4de,0xfffff9f9,24,MayaUi.BORDER));
        temperature=MayaUi.text(c,"--°",27,MayaUi.INK,true);
        addView(temperature,new LayoutParams(MayaUi.dp(c,92),-2));
        LinearLayout middle=new LinearLayout(c);middle.setOrientation(VERTICAL);
        conditions=MayaUi.text(c,"Tap to fetch local weather",15,MayaUi.INK,true);
        middle.addView(conditions);
        TextView hint=MayaUi.text(c,"Location permission is optional",12,MayaUi.MUTED,false);
        middle.addView(hint);
        addView(middle,new LayoutParams(0,-2,1f));
        icon=MayaUi.center(c,"☀",29,MayaUi.CORAL,false);
        addView(icon,new LayoutParams(MayaUi.dp(c,38),-2));
        setClickable(true);setFocusable(true);setContentDescription("Weather. Tap to enable location-based forecast.");
        setOnClickListener(v->requestLocation.run());
    }
    public void showError(String message){
        temperature.setText("--°");conditions.setText(message);loading=false;
    }
    public void load(Location location){
        if(location==null){showError("Location unavailable • Tap to retry");return;}
        if(loading)return;
        loading=true;conditions.setText("Fetching local weather…");
        final double lat=location.getLatitude(),lon=location.getLongitude();
        network.execute(()->{
            HttpURLConnection conn=null;
            try{
                String url="https://api.open-meteo.com/v1/forecast?latitude="+
                    String.format(Locale.US,"%.4f",lat)+"&longitude="+
                    String.format(Locale.US,"%.4f",lon)+
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto";
                conn=(HttpURLConnection)new URL(url).openConnection();
                conn.setConnectTimeout(8500);conn.setReadTimeout(8500);
                conn.setRequestProperty("User-Agent","MAYA-AI-Voice-Assistant/0.4");
                if(conn.getResponseCode()!=200)throw new Exception("Weather service unavailable");
                StringBuilder result=new StringBuilder();
                try(BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()))){
                    String line;while((line=reader.readLine())!=null)result.append(line);
                }
                JSONObject current=new JSONObject(result.toString()).getJSONObject("current");
                double temp=current.getDouble("temperature_2m");
                double feel=current.getDouble("apparent_temperature");
                int humidity=current.getInt("relative_humidity_2m");
                int code=current.getInt("weather_code");
                final String state=describe(code);
                ui.post(()->{
                    temperature.setText(Math.round(temp)+"°C");
                    conditions.setText(state+" • Feels "+Math.round(feel)+"° • "+humidity+"% humidity");
                    icon.setText(code==0?"☀":code>=51?"☂":"☁");
                    loading=false;
                });
            }catch(Exception e){ui.post(()->showError("Weather unavailable • Tap to retry"));}
            finally{if(conn!=null)conn.disconnect();}
        });
    }
    private String describe(int c){
        if(c==0)return "Clear";
        if(c<=3)return "Partly cloudy";
        if(c<=48)return "Fog";
        if(c<=57)return "Drizzle";
        if(c<=67)return "Rain";
        if(c<=77)return "Snow";
        if(c<=82)return "Showers";
        if(c<=86)return "Snow showers";
        if(c>=95)return "Thunderstorm";
        return "Cloudy";
    }
}
