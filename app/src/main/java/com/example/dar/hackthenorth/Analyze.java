package com.example.dar.hackthenorth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import java.io.File;

public class Analyze extends AppCompatActivity {

    protected String _tag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
      //  if(getIntent())
        String receipt = getIntent().getStringExtra("receipt");
        Log.i(_tag, "PATH: " + receipt);
        File imageFile = new File(receipt);
        
        //ITesseract instance = new Tesseract(); // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping

        /*try {
            String result = instance.doOCR(imageFile);
            Log.d("MESSAGE", result);
        } catch (TesseractException e) {
            Log.d("MESSAGE", e.getMessage());
        }*/

        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(receipt, options);

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setImage(bitmap);*/
        //TessBaseAPI baseApi = new TessBaseAPI();
        //baseApi.setImage(imageFile);

        try {
            //String recognizedText = baseApi.getUTF8Text();
            //Log.d("MESSAGE", "OCRed Text: " + recognizedText);
        } catch (Exception e) {
            Log.d("MESSAGE", e.getMessage());
        }
        //String recognizedText = baseApi.getUTF8Text();

        //baseApi.end();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_analyze, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
