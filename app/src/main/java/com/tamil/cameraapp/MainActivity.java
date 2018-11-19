package com.tamil.cameraapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 102;

    private ImageView imgCamera;
    private String strImagePath = "";
    public String temp = "";

    private static final int REQUEST_CAMERA = 5555;
    public static final int REQUEST_IMAGES = 1111;
    public static final int SELECT_FILE = 2222;
    public static final int SELECT_DOC = 3333;
    public String string_filetype = "";

    String string_DIALOG_MENU_TAKEPHOTO = "Take Photo";
    String string_DIALOG_MENU_CHOOSEIMAGE = "Choose from Gallery";
    String string_DIALOG_MENU_CHOOSEFILES = "Choose Docs";
    String string_DIALOG_MENU_CANCEL = "Cancel";

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

        final CharSequence[] chararrOptions = {string_DIALOG_MENU_TAKEPHOTO, string_DIALOG_MENU_CHOOSEIMAGE, string_DIALOG_MENU_CANCEL};


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,
                AlertDialog.THEME_HOLO_LIGHT);
        builder.setItems(chararrOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (chararrOptions[item].equals(string_DIALOG_MENU_TAKEPHOTO)) {

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




                } else if (chararrOptions[item].equals(string_DIALOG_MENU_CHOOSEIMAGE)) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    (MainActivity.this).startActivityForResult(Intent.createChooser(intent, string_DIALOG_MENU_CHOOSEIMAGE), SELECT_FILE);
                }

                else if (chararrOptions[item].equals(string_DIALOG_MENU_CANCEL)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();


    }

    //OnActivity Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CAMERA) {
//
//                Bitmap thumbnail = BitmapFactory.decodeFile(strImagePath);
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
//                byte[] imageBytes = bytes.toByteArray();
//                String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//                imgCamera.setImageBitmap(thumbnail);
//
//            }
//        }

        switch (requestCode) {
            case SELECT_DOC:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        //Display an error
                        return;
                    } else {
                        Uri uri = data.getData();
                        try {
                            File file_docs = new File(uri.getPath());
                            int size = (int) file_docs.length();

                            ContentResolver cR = getApplicationContext().getContentResolver();
                            string_filetype = MimeTypeMap.getFileExtensionFromUrl(uri.toString());

                            byte[] bytes = new byte[size];
                            try {
                                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file_docs));
                                buf.read(bytes, 0, bytes.length);
                                buf.close();
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            temp = Base64.encodeToString(bytes, Base64.DEFAULT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        return;
                    } else {
                        Uri uri = data.getData();
                        try {
                            ContentResolver cR = getApplicationContext().getContentResolver();
                            string_filetype = "png";

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            Bitmap scaledBitmap = scaleDown(bitmap, 1024, true);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            scaledBitmap.compress(
                                    Bitmap.CompressFormat.JPEG, 90, baos);
                            byte[] b = baos.toByteArray();
                            temp = "";
                            temp = Base64.encodeToString(b, Base64.DEFAULT);
                            imgCamera.setImageBitmap(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {

                    if (requestCode == REQUEST_CAMERA) {
//                        strImagePath = Environment.getExternalStorageDirectory() + "/i2i_ifazidesk/Images/" + strImagename;
                        Log.i("Imagepath", "PATH : " + strImagePath);
                        File objFile = new File(strImagePath);
                        Bitmap objBitmap = null;

                        Uri uri = Uri.parse(strImagePath);
                        ContentResolver cR = getApplicationContext().getContentResolver();
                        string_filetype = "png";
                        try {
                            try {
                                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                                btmapOptions.inSampleSize = 5;
                                objBitmap = BitmapFactory.decodeFile(objFile.getAbsolutePath(), btmapOptions);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                System.gc();
                                try {
                                    objBitmap = BitmapFactory.decodeFile(objFile.getAbsolutePath());
                                } catch (OutOfMemoryError e2) {
                                    e2.printStackTrace();
                                }
                            }
                            Bitmap scaledBitmap = scaleDown(objBitmap, 1024, true);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            scaledBitmap.compress(
                                    Bitmap.CompressFormat.JPEG, 90, baos);
                            byte[] b = baos.toByteArray();
                            temp = "";
                            temp = Base64.encodeToString(b,
                                    Base64.DEFAULT);
                            temp = "";
                            temp = Base64.encodeToString(b, Base64.DEFAULT);
                            imgCamera.setImageBitmap(objBitmap);
                            try {
                                objFile.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,boolean filter) {
        Bitmap newBitmap;
        float floatRatio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int intWidth = Math.round(floatRatio * realImage.getWidth());
        int intHeight = Math.round(floatRatio * realImage.getHeight());

        float maxHeight = 2048.0f;
        float maxWidth = 2048.0f;

        int intmaxheight = Math.round(maxHeight);
        int intmaxwidth = Math.round(maxWidth);

        if (intWidth > maxHeight || intHeight > maxWidth) {
            newBitmap = Bitmap.createScaledBitmap(realImage, intmaxwidth,
                    intmaxheight, filter);
        } else {
            newBitmap = Bitmap.createScaledBitmap(realImage, intWidth,
                    intHeight, filter);
        }

        return newBitmap;
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
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
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

