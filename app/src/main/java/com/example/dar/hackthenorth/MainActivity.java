package com.example.dar.hackthenorth;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;
import com.opencsv.CSVWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public String sTitle = null;
    public String sTotal = null;
    public String sAmount = null;
    public String[] sProduct = null;
    public String[] sProdCost = null;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    protected String TAG = "LOG MESSAGE: !!!!!";
    //Button takePic = (Button) this.findViewById(R.id.takePic);
    //Button selectPic = (Button) this.findViewById(R.id.selectPic);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //test();
        //TestTwo();
        //TestThree();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                //return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public void goToCamera(View view){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        //camera.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(camera, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        data.getDataString(), Toast.LENGTH_LONG).show();

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File finalFile = new File(getRealPathFromURI(data.getData()));

                AsyncHttpPost post = new AsyncHttpPost("http://23.251.152.45:3030/upload");
                MultipartFormDataBody body = new MultipartFormDataBody();
                body.addFilePart("fileUploaded", finalFile);
                post.setBody(body);
                AsyncHttpClient.getDefaultInstance().execute(post, new HttpConnectCallback() {
                    @Override
                    public void onConnectCompleted(Exception ex, AsyncHttpResponse response) {
                        if (ex != null) {
                            Log.e(TAG, "multipart network error", ex);
                            return;
                        }
                        onUploadComplete(response.message());
                    }
                });

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

    }

    private void onUploadComplete(String rawText) {

        Pattern titleP = Pattern.compile("\\s*.*?([a-zA-Z].*[a-zA-Z]).*?\\s*.*");
        Pattern totalP = Pattern.compile(".*(total|Total|TOTAL).*?(\\d*\\.\\d\\d)$");
        Pattern amountP = Pattern.compile(".*(amount|Amount|AMOUNT).*?(\\d*\\.\\d\\d)$");
        Pattern productP = Pattern.compile("\\s*\\d*\\s*(.*?)\\s*(\\d*\\.\\d\\d)$");

        String title = null;
        String totalVal = null;
        String sTotal = null;
        Map<String, String> products = new HashMap<>();

        String[] lines = rawText.split("\n");

        Matcher titleM = titleP.matcher(lines[0]); // Attempt to match only first line
        if (titleM.matches()) title = titleM.group(1);

        int totalLineIndex = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            Log.i(TAG, lines[i]);
            Matcher totalM = totalP.matcher(lines[i]);
            if (totalM.matches()) {
                Log.i(TAG, "Matched. Totals " + totalM.group(1) + " " + totalM.group(2));
                totalLineIndex = i;
                sTotal = totalM.group(1);
                totalVal = totalM.group(2);
                break;
            }

            Matcher amountM = amountP.matcher(lines[i]);
            if (amountM.matches()) {
                Log.i(TAG, "Matched. Amounts " + amountM.group(1) + " " + amountM.group(2));
                totalLineIndex = i;
                totalVal = amountM.group(2);
                break;
            }
        }

        for (int i = 1; i < lines.length && i < totalLineIndex; i++) {
            Matcher productM = productP.matcher(lines[i]);
            if (productM.matches()) {
                products.put(productM.group(1), productM.group(2));
            }
        }


        Set mapSet = (Set) products.entrySet();
        Iterator mapIterator = mapSet.iterator();

        System.out.println(title);

        while (mapIterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) mapIterator.next();
            // getKey Method of HashMap access a key of map
            String keyValue = (String) mapEntry.getKey();
            //getValue method returns corresponding key's value
            String value = (String) mapEntry.getValue();
            System.out.println("Key : " + keyValue + "= Value : " + value);
        }
        try{
            exportQB(title, sTotal, totalVal, products);
        } catch(IOException e) {
            Log.d(TAG, "Parsing of Individual Products Failed!");
        }

        System.out.println(sTotal);
        System.out.println(totalVal);

    }

    /*protected void TestThree (){
        String rawText = "La Tasca\n" +
                "\n" +
                "¥:::::SHMMH4HSMR&RﬂMUMNf2:221\n" +
                "523 HIGH ST.KINGSTON-UPON-THAMES\n" +
                "KT1 1EU\n" +
                "\n" +
                "Tel: 0208 439 1002 Fax: 0208 439 1003\n" +
                "VAT No: 693200741\n" +
                "\n" +
                "CHK# 10 TBL# 30\n" +
                "\n" +
                "SUPER STEF # 103\n" +
                "\n" +
                "03/03/07 12:54:21 GUESTS 2\n" +
                "1 Half Cruzcampo 1.75\n" +
                "1 Orange Juice 2.45\n" +
                "1 Menu Para Ninos 3.95\n" +
                "1 Pan de Ajo 2.75\n" +
                "1 R-Patatas Pobre 2.95\n" +
                "1 Croquatas Champin 3.75\n" +
                "1 Montado Jamon ﬂue 3.95\n" +
                "1 Calamaras 3.95\n" +
                "1 Cafe Doble 2.00\n" +
                "1 Cappuccino 1.90\n" +
                "1 Tarta Naranja 3.95\n" +
                "\n" +
                "Terminal 5 T#0000025 --------- -~\n" +
                "\n" +
                "TOTAL: 33.35\n" +
                "\n" +
                "VAT 4.97\n" +
                "\n" +
                "Last Serviced 03/03/07 13:56:54\n" +
                "\n" +
                "SERVICE NOT INCL\n" +
                "\n" +
                "A 10% DISCRETIONARY SERVICE CHARGE WILL\n" +
                "BE ADDED FOR PARTIES OF 8 OR MORE\n" +
                "GRATUITY AT YOUR DISCRETION\n" +
                "BOOK NOW FOR\n" +
                "h1EJ1Ff1E5F1 '53 [)l\\\\f 1 1 I";
        Pattern titleP = Pattern.compile("\\s*.*?([a-zA-Z].*[a-zA-Z]).*?\\s*.*");
        Pattern totalP = Pattern.compile(".*(total|Total|TOTAL).*?(\\d*\\.\\d\\d)$");
        Pattern amountP = Pattern.compile(".*(amount|Amount|AMOUNT).*?(\\d*\\.\\d\\d)$");
        Pattern productP = Pattern.compile("\\s*\\d*\\s*(.*?)\\s*(\\d*\\.\\d\\d)$");

        String title = null;
        String totalVal = null;
        String sTotal = null;
        Map<String, String> products = new HashMap<>();

        String[] lines = rawText.split("\n");

        Matcher titleM = titleP.matcher(lines[0]); // Attempt to match only first line
        if (titleM.matches()) title = titleM.group(1);

        int totalLineIndex = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            Log.i(TAG, lines[i]);
            Matcher totalM = totalP.matcher(lines[i]);
            if (totalM.matches()) {
                Log.i(TAG, "Matched. Totals " + totalM.group(1) + " " + totalM.group(2));
                totalLineIndex = i;
                sTotal = totalM.group(1);
                totalVal = totalM.group(2);
                break;
            }

            Matcher amountM = amountP.matcher(lines[i]);
            if (amountM.matches()) {
                Log.i(TAG, "Matched. Amounts " + amountM.group(1) + " " + amountM.group(2));
                totalLineIndex = i;
                totalVal = amountM.group(2);
                break;
            }
        }

        for (int i = 1; i < lines.length && i < totalLineIndex; i++) {
            Matcher productM = productP.matcher(lines[i]);
            if (productM.matches()) {
                products.put(productM.group(1), productM.group(2));
            }
        }

        Log.d("sTITLE", title);
        Log.d("sTOTAL", sTotal);
        Log.d("sAMOUNT", totalVal);
        Set mapSet = (Set) products.entrySet();
        Iterator mapIterator = mapSet.iterator();
        System.out.println("Display the key/value of HashMap.");
        while (mapIterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) mapIterator.next();
            // getKey Method of HashMap access a key of map
            String keyValue = (String) mapEntry.getKey();
            //getValue method returns corresponding key's value
            String value = (String) mapEntry.getValue();
            System.out.println("Key : " + keyValue + "= Value : " + value);
        }
        try{
            exportQB(title, sTotal, totalVal, products);
        } catch(IOException e) {
            Log.d(TAG, "exportQB Failed");
        }
    }*/

    protected void exportQB(String x, String y, String z, Map<String, String> abc) throws IOException{
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "csvname.csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            String title = x, sTotal = y, totalVal = z;
            Map<String, String> products = abc;


            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {title});

            Set mapSet = (Set) products.entrySet();
            Iterator mapIterator = mapSet.iterator();
            while (mapIterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) mapIterator.next();
                // getKey Method of HashMap access a key of map
                String keyValue = (String) mapEntry.getKey();
                //getValue method returns corresponding key's value
                String value = (String) mapEntry.getValue();
                data.add(new String[]{keyValue, value});
            }

            data.add(new String[]{sTotal, totalVal});
            csvWrite.writeAll(data);
        }catch(Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }
}
