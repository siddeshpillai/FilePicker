package com.example.filepicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final String TAG = "FilePicker";

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        try {
            if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = null;

                if (resultData != null) {
                    uri = resultData.getData();
                    Log.i(TAG, "Uri: " + uri.toString());
                    Log.i(TAG, "Path: " + uri.getPath());
                    Log.i(TAG, "isVirtualFile " + isVirtualFile(uri));
                    getMetaData(uri);

                    String mimeType = getContentResolver().getType(uri);
                    Log.i(TAG, "mimeType " + mimeType);

                    ContentResolver contentResolver = MainActivity.this.getContentResolver();

                    String name = getDisplayName(contentResolver, uri);
                    if (checkPermission())
                    {
                        // Code for above or equal 23 API Oriented Device
                        // Your Permission granted already .Do next code
                    } else {
                        requestPermission(); // Code for permission
                    }

                    Log.i(TAG, "file copied " + copyFileFromUri(MainActivity.this, uri, name));

//
//                    String mediaType = contentResolver.getType(uri);
//                    if (mediaType == null || mediaType.isEmpty()) {
//                        mediaType = "application/octet-stream";
//                    }
//
//                    byte[] bytes = getBytesFromInputStream(contentResolver.openInputStream(uri));
//
//                    String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
//
//                    JSONObject result = new JSONObject();
//
//                    result.put("data", base64);
//                    result.put("mediaType", mediaType);
//                    result.put("name", name);
//                    result.put("uri", uri.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean copyFileFromUri(Context context, Uri fileUri, String fileName)
    {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {
            ContentResolver content = context.getContentResolver();
            inputStream = content.openInputStream(fileUri);

            File root = Environment.getExternalStorageDirectory();
            if(root == null){
                Log.d(TAG, "Failed to get root");
            }

            // create a directory
            File saveDirectory = new File(Environment.getExternalStorageDirectory()+File.separator+"appcache" +File.separator);
            System.out.println("Directory Path " + Environment.getExternalStorageDirectory()+File.separator+"appcache" +File.separator);
            // create direcotory if it doesn't exists
            if (saveDirectory.mkdirs())
            {
                Log.i(TAG, "Directory created");
            }

            Log.i(TAG, "Save Directory " + saveDirectory.getAbsolutePath());

            outputStream = new FileOutputStream( saveDirectory + File.separator + fileName); // filename.png, .mp3, .mp4 ...
            if(outputStream != null) {
                Log.e( TAG, "Output Stream Opened successfully");
            }

            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            while (inputStream.read( buffer, 0, buffer.length ) >= 0 )
            {
                System.out.println("Writing");
                outputStream.write( buffer, 0, buffer.length );
            }

            inputStream.close();
            outputStream.close();

        } catch ( Exception e ){
            Log.e( TAG, "Exception occurred " + e.getMessage());
        } finally{

        }
        return true;
    }

    public static byte[] getBytesFromInputStream (InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];

        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public static String getDisplayName (ContentResolver contentResolver, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor metaCursor = contentResolver.query(uri, projection, null, null, null);

        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    return metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        return "File";
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private boolean isVirtualFile(Uri uri) {
        if (!DocumentsContract.isDocumentUri(this, uri)) {
            return false;
        }

        Cursor cursor = getContentResolver().query(
                uri,
                new String[] { DocumentsContract.Document.COLUMN_FLAGS },
                null, null, null);

        int flags = 0;
        if (cursor.moveToFirst()) {
            flags = cursor.getInt(0);
        }
        cursor.close();

        return (flags & DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0;
    }

    public void getMetaData(Uri uri) {

        Cursor cursor = MainActivity.this.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {

                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
