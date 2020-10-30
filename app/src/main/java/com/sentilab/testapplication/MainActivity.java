package com.sentilab.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;

    // buttons
    private Button btn_login;
    private BackPressHandler backPressHandler;
    private Button CameraButton;
    private Button UploadButton;

    // for permission requests
    public static final int REQUEST_PERMISSION = 300;

    // request code for permission requests to the os for image
    public static final int REQUEST_IMAGE = 100;

    // will hold uri of image obtaind from gallery
    private Uri imageUri;

    // string to send to next activity that describes the chosen classifier
    private String chosen;

    // boolean value dictating if chosen model is quantized version or not.
    private boolean quant;

    private String filePath;
    private static Bitmap photo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backPressHandler = new BackPressHandler(this);

        // 로그인 버튼 클릭 시
        btn_login = findViewById(R.id.login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        // 외부 저장소에 권한 필요, 동적 퍼미션
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 10);
            }
        }


//        ///////////////////////******************** 시작 ******************///////////////
//        // request permission to use the camera on the user's phone
//        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, REQUEST_PERMISSION);
//        }
//
//        // request permission to write data (aka images) to the user's external storage of their phone
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_PERMISSION);
//        }
//
//        // request permission to read data (aka images) from the user's external storage of their phone
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    REQUEST_PERMISSION);
//        }


        UploadButton = findViewById(R.id.upload_btn);
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakeAlbumAction();
            }
        });


        CameraButton = findViewById(R.id.camera_btn);
        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakePhotoAction();
            }
        });
    }

    /**
     * 카메라에서 사진 촬영
     */
    public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기
    {
        // filename in assets
        chosen = "model_unquant.tflite";
//        chosen = "model.tflite";

        // model in not quantized
        quant = false;
//        quant = true;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        // tell camera where to store the resulting picture
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Log.d("!@#$!@#$IMAGEURI", imageUri.toString());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // start camera, and wait for it to finish
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    // checks that the user has allowed all the required permission of read and write and camera. If not, notify the user and close the application
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "This application needs read, write, and camera permissions to run. Application now closing.", Toast.LENGTH_LONG);
                System.exit(0);
            }
        }
    }


    /**
     * 앨범에서 이미지 가져오기
     */
    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기
    {
        // filename in assets
        chosen = "model_unquant.tflite";
//        chosen = "model.tflite";

        // model in not quantized
        quant = false;
//        quant = true;

        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
//        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                // 이후의 처리가 카메라와 같으므로 일단 break없이 진행한다.
                imageUri = data.getData();
                Log.d("FoodImage", imageUri.getPath());
            }
            case PICK_FROM_CAMERA: {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭 어플리케이션을 호출
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri, "image/*");

                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE); // CROP_FROM_CAMERA case문 이동
                break;
            }
            case CROP_FROM_iMAGE: {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                if (resultCode != RESULT_OK) {
                    return;
                }

                final Bundle extras = data.getExtras();


                if (extras != null) {
                    photo = extras.getParcelable("data");

                    Intent i = new Intent(MainActivity.this, FoodClassify.class);
                    // 로그인을 했는지
                    i.putExtra("logined", "no");
                    // put image data in extras to send
                    i.putExtra("resID_uri", photo);
                    Log.d("RESID_URI", String.valueOf(photo));
                    // put filename in extras
                    Log.d("chosen", chosen);
                    i.putExtra("chosen", chosen);
                    // put model type in extras
                    Log.d("quant", String.valueOf(quant));
                    i.putExtra("quant", quant);
                    // send other required data
                    startActivity(i);
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        // Default
        backPressHandler.onBackPressed();
    }


}
