package com.tanimworld.maya;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Sends only a specifically chosen image to Gemini after explicit on-screen approval. */
public final class ImageVisionActivity extends Activity {
    private static final int PICK=67;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private TextView output;
    private EditText request;
    private Uri chosen;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff070b1d);
        LinearLayout col=new LinearLayout(this);col.setPadding(24,35,24,26);col.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(col);
        output=new TextView(this);output.setTextColor(Color.WHITE);output.setTextSize(16);
        output.setText("MAYA Image Vision (consent required)\n\nChoose a photo/screenshot. It will be sent to Google Gemini ONLY after you approve. Avoid passwords, IDs or private messages.");
        col.addView(output);
        request=new EditText(this);request.setSingleLine(false);request.setMinLines(2);request.setTextColor(Color.WHITE);
        request.setText("Describe what is visible in this image, and mention any uncertain details.");
        col.addView(request);
        Button choose=new Button(this);choose.setText("CHOOSE IMAGE / SCREENSHOT");
        choose.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");
            startActivityForResult(i,PICK);
        });col.addView(choose);
        Button send=new Button(this);send.setText("REVIEW AND ASK GEMINI");
        send.setOnClickListener(v->review());col.addView(send);
        setContentView(scroll);
    }
    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data);
        if(req==PICK&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            chosen=data.getData();output.setText("Image selected. It has NOT been uploaded. Review your question, then approve.");
        }
    }
    private void review() {
        if(chosen==null){output.setText("Choose an image first.");return;}
        if(SecretStore.getApiKey(this).isEmpty()){output.setText("Set Gemini API key in MAYA Settings first.");return;}
        final Uri selection=chosen;
        final String prompt=request.getText().toString().trim();
        if(prompt.isEmpty()){output.setText("Type a question about the image first.");return;}
        new AlertDialog.Builder(this).setTitle("Send this image to Google?")
            .setMessage("MAYA will upload the selected image to Gemini 3.8 Flash for analysis. The response may contain errors. Continue only if you have permission to share it.")
            .setNegativeButton("CANCEL",null)
            .setPositiveButton("SEND TO GEMINI",(d,w)->analyze(selection,prompt)).show();
    }
    private void info(String text){runOnUiThread(()->output.setText(text));}
    private void analyze(Uri uri,String prompt){
        info("Preparing selected image...");
        worker.execute(()->{
            try {
                String apiKey=SecretStore.getApiKey(this);
                if(apiKey.isEmpty())throw new Exception("API key unavailable");
                byte[] image=resize(uri);
                if(image.length>2_000_000)throw new Exception("Image too large after resizing");
                JSONObject partImage=new JSONObject().put("inline_data",new JSONObject()
                        .put("mime_type","image/jpeg").put("data",Base64.encodeToString(image,Base64.NO_WRAP)));
                JSONObject partText=new JSONObject().put("text",prompt);
                JSONObject body=new JSONObject().put("contents",new JSONArray().put(
                        new JSONObject().put("role","user").put("parts",new JSONArray().put(partText).put(partImage))));
                OkHttpClient http=new OkHttpClient.Builder().callTimeout(50,TimeUnit.SECONDS).build();
                Request requestHttp=new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent")
                        .header("x-goog-api-key",apiKey)
                        .post(RequestBody.create(body.toString(),MediaType.get("application/json; charset=utf-8"))).build();
                try(Response response=http.newCall(requestHttp).execute()){
                    String text=response.body()==null?"":response.body().string();
                    if(!response.isSuccessful())throw new Exception("Gemini HTTP "+response.code()+". Check your key/model.");
                    JSONObject json=new JSONObject(text);
                    JSONArray candidates=json.optJSONArray("candidates");
                    if(candidates==null||candidates.length()==0)throw new Exception("No answer returned");
                    JSONArray parts=candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts");
                    StringBuilder answer=new StringBuilder();
                    for(int i=0;i<parts.length();i++)answer.append(parts.getJSONObject(i).optString("text"));
                    info(answer.length()==0?"No descriptive text in response.":"Gemini's image response (may be inaccurate):\n"+answer);
                }
            }catch(Exception e){info("Image analysis failed: "+e.getMessage());}
        });
    }
    private byte[] resize(Uri uri)throws Exception {
        BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;
        try(InputStream stream=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(stream,null,opts);}
        int scale=1;
        while(opts.outWidth/scale>1600||opts.outHeight/scale>1600)scale*=2;
        opts.inJustDecodeBounds=false;opts.inSampleSize=scale;
        Bitmap bitmap;
        try(InputStream stream=getContentResolver().openInputStream(uri)){bitmap=BitmapFactory.decodeStream(stream,null,opts);}
        if(bitmap==null)throw new Exception("Unsupported image");
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,75,buffer);bitmap.recycle();
        return buffer.toByteArray();
    }
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
