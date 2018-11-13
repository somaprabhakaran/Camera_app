package com.tamil.cameraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int REQUEST_CAMERA = 5555;
    private ImageView imgCamera;
    private String strImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCamera = findViewById(R.id.imgCamera);

        //BtnCamera click listener
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        requestAppPermissions();
    }


    //Select image using File Provider
    public void selectImage() {

        final String dir = Environment.getExternalStorageDirectory() + "/CameraApp/Images/";

        if (Build.VERSION.SDK_INT >= 24) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File image = new File(dir, "temp.png");
            strImagePath = image.getAbsolutePath();

            Log.e("StrImagePath", strImagePath);
            Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this, "com.tamil.cameraapp.fileprovider", image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            (MainActivity.this).startActivityForResult(intent, REQUEST_CAMERA);


        } else {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File f = new File(dir, "temp.png");
            strImagePath = f.getAbsolutePath();

            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            (MainActivity.this).startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    //OnActivity Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {

                Bitmap thumbnail = BitmapFactory.decodeFile(strImagePath);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
                byte[] imageBytes = bytes.toByteArray();
                String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                imgCamera.setImageBitmap(thumbnail);

            }
        }
    }


    //Add new method for app permission
    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }, STORAGE_REQUEST_CODE); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case STORAGE_REQUEST_CODE:

                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (storageAccepted) {
                    File camera_Folder = new File(Environment.getExternalStorageDirectory() + "/CameraApp/");
                    File camera_ImageFolder = new File(Environment.getExternalStorageDirectory() + "/CameraApp/Images/");

                    if (!camera_Folder.exists()) {
                        if (camera_Folder.mkdir()) ; //directory is created;
                    }
                    if (!camera_ImageFolder.exists()) {
                        if (camera_ImageFolder.mkdir()) ; //directory is created;
                    }
                }
                break;
        }
    }
}
