package com.tanimworld.maya;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Explicit user-selected image/PDF OCR and barcode scanner. No hidden camera use or uploads. */
public final class DocumentToolsActivity extends Activity {
    private final ExecutorService work = Executors.newSingleThreadExecutor();
    private TextView output;
    private String lastText="";
    private static final int IMAGE=51, PDF=52, CODE=53;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff070b1d);
        LinearLayout column=new LinearLayout(this);column.setPadding(22,30,22,28);column.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(column);
        output=new TextView(this);output.setTextColor(Color.WHITE);output.setTextSize(16);
        output.setText("MAYA • Document Tools\n\nSelect an image or PDF yourself. Text recognition runs on-device with Google ML Kit (Latin script). Review medical labels and receipt totals manually.");
        column.addView(output);
        add(column,"READ TEXT FROM PHOTO",()->pick("image/*",IMAGE));
        add(column,"READ PDF (FIRST 6 PAGES)",()->pick("application/pdf",PDF));
        add(column,"SCAN QR / BARCODE FROM IMAGE",()->pick("image/*",CODE));
        add(column,"SHARE RECOGNIZED TEXT",()->{
            if(lastText.isEmpty()){output.setText("Read a document first.");return;}
            Intent share=new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");share.putExtra(Intent.EXTRA_TEXT,lastText);
            startActivity(Intent.createChooser(share,"Share extracted text"));
        });
        setContentView(scroll);
    }
    private void add(LinearLayout col,String title,Runnable run){
        Button button=new Button(this);button.setAllCaps(false);button.setText(title);
        button.setOnClickListener(v->run.run());col.addView(button);
    }
    private void pick(String mime,int request){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType(mime);
        startActivityForResult(intent,request);
    }
    private void status(String s){runOnUiThread(()->output.setText(s));}
    @Override protected void onActivityResult(int request,int code,Intent data){
        super.onActivityResult(request,code,data);
        if(code!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();lastText="";status("Reading selected file locally…");
        work.execute(()->{
            try{
                if(request==PDF){readPdf(uri);return;}
                Bitmap bmp=decode(uri);
                if(bmp==null)throw new Exception("Cannot decode selected image");
                if(request==CODE)scanCode(bmp);
                else recognize(new Bitmap[]{bmp});
            }catch(Exception e){status("Document read failed: "+e.getMessage());}
        });
    }
    private Bitmap decode(Uri uri)throws Exception {
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
        try(InputStream input=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(input,null,options);}
        int scale=1;
        while(options.outWidth/scale>2048||options.outHeight/scale>2048)scale*=2;
        options.inJustDecodeBounds=false;options.inSampleSize=scale;
        try(InputStream input=getContentResolver().openInputStream(uri)){return BitmapFactory.decodeStream(input,null,options);}
    }
    private void readPdf(Uri uri)throws Exception {
        List<Bitmap> pages=new ArrayList<>();
        try(ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(uri,"r")) {
            if(fd==null)throw new Exception("Cannot open PDF");
            try(PdfRenderer renderer=new PdfRenderer(fd)){
                int limit=Math.min(6,renderer.getPageCount());
                if(limit==0)throw new Exception("PDF contains no pages");
                for(int i=0;i<limit;i++){
                    try(PdfRenderer.Page page=renderer.openPage(i)){
                        float ratio=Math.min(1.8f,Math.min(1650f/page.getWidth(),2100f/page.getHeight()));
                        int width=Math.max(1,Math.round(page.getWidth()*ratio));
                        int height=Math.max(1,Math.round(page.getHeight()*ratio));
                        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(Color.WHITE);
                        page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        pages.add(bitmap);
                    }
                }
            }
        }
        recognize(pages.toArray(new Bitmap[0]));
    }
    private void recognize(Bitmap[] pages){
        com.google.mlkit.vision.text.TextRecognizer recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        String[] texts=new String[pages.length];
        final int[] remaining={pages.length};
        final Object lock=new Object();
        for(int i=0;i<pages.length;i++){
            final int index=i;Bitmap bitmap=pages[i];
            recognizer.process(InputImage.fromBitmap(bitmap,0))
                .addOnSuccessListener(result->complete(index,result.getText(),texts,remaining,lock,recognizer))
                .addOnFailureListener(error->complete(index,"[Page unreadable: "+error.getMessage()+"]",texts,remaining,lock,recognizer));
        }
    }
    private void complete(int index,String value,String[] texts,int[] remaining,Object lock,com.google.mlkit.vision.text.TextRecognizer recognizer){
        synchronized(lock){
            texts[index]=value;
            remaining[0]--;
            if(remaining[0]!=0)return;
            recognizer.close();
            StringBuilder result=new StringBuilder();
            for(int i=0;i<texts.length;i++)result.append("\n[Page ").append(i+1).append("]\n").append(texts[i]==null?"":texts[i]).append("\n");
            lastText=result.toString();
            Matcher total=Pattern.compile("(?im)^.*(?:total|amount due|grand total).*?([0-9]+[.,][0-9]{2})").matcher(lastText);
            String note=total.find()?"\nPossible receipt total: "+total.group(1)+" (verify yourself).":"";
            status("OCR complete.\n"+lastText+note);
        }
    }
    private void scanCode(Bitmap bitmap) {
        com.google.mlkit.vision.barcode.BarcodeScanner scanner=BarcodeScanning.getClient();
        scanner.process(InputImage.fromBitmap(bitmap,0))
          .addOnSuccessListener(codes->{
              StringBuilder sb=new StringBuilder();
              for(Barcode code:codes)sb.append(code.getRawValue()).append("\n");
              scanner.close();lastText=sb.toString();
              status(codes.isEmpty()?"No barcode detected.":"Found barcodes (review any links before opening):\n"+lastText);
          })
          .addOnFailureListener(error->{scanner.close();status("Barcode scanning failed: "+error.getMessage());});
    }
    @Override protected void onDestroy(){work.shutdown();super.onDestroy();}
}
