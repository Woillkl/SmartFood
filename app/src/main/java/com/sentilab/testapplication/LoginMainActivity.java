package com.sentilab.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginMainActivity extends AppCompatActivity {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;

    // active util
    private TextView txt_username;
    private Button CameraButton, mypageButton, UploadButton, GalleryButton;

    // intent로 부터 넘겨받은 UserName
    private String UserName;

    private BackPressHandler backPressHandler;

    // will hold uri of image obtaind from gallery
    private Uri imageUri;

    // string to send to next activity that describes the chosen classifier
    private String chosen;

    // boolean value dictating if chosen model is quantized version or not.
    private boolean quant;

    // for permission requests
    public static final int REQUEST_PERMISSION = 300;

    private String filePath;
    private static Bitmap photo = null;

    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        backPressHandler = new BackPressHandler(this);

        // 아이디를 전 intent로 부터 받는다.
        txt_username = findViewById(R.id.txt_username);
        //TextView에 아이디 Set
        UserName = getIntent().getStringExtra("ID");
        txt_username.setText(UserName + " 님");


        // 로그아웃 버튼 클릭 시
        final TextView logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginMainActivity.this);
                builder.setMessage("정말 로그아웃 하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(LoginMainActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //기존에 쌓여있던 스택을 모두 없앤다.
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // task를 새로 생성한다
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                builder.show();

            }
        });


        // 마이페이지 버튼 클릭시
        mypageButton = findViewById(R.id.btn_mypage);
        mypageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginMainActivity.this, MyPageActivity.class);
                if (UserName.equals("Administrator")) {
                    intent.putExtra("UserName", "admin");
                    startActivity(intent);
                } else {
                    intent.putExtra("UserName", UserName);
                    startActivity(intent);
                }

            }
        });


        // 카메라 버튼 클릭시
        CameraButton = findViewById(R.id.camera_btn);
        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakePhotoAction();
            }
        });


        // 업로드 버튼 클릭시
        UploadButton = findViewById(R.id.upload_btn);
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakeAlbumAction();
            }
        });

        // 갤러리 버튼 클릭시
        GalleryButton = findViewById(R.id.gallery_btn);
        GalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UserName.equals("Administrator")) {
                    Intent intent = new Intent(LoginMainActivity.this, AdminGalleryFoodList.class);
                    intent.putExtra("UserName", "admin");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginMainActivity.this, ListDateActivity.class);
                    intent.putExtra("UserName", UserName);
                    startActivity(intent);
                }
            }
        });

    } // 메인 함수 끝


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
                imgPath = getRealPathFromUri(imageUri);

            }
            case PICK_FROM_CAMERA: {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭 어플리케이션을 호출
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri, "image/*");
                Log.d("$$$CAMERAURI", imageUri.getPath());
                imgPath = getRealPathFromUri(imageUri);


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

                    Intent i = new Intent(LoginMainActivity.this, FoodClassify.class);
                    // 로그인을 했는지 안했는지 보낸다.
                    i.putExtra("logined", "yes");
                    // UserName를 extras로 보낸다
                    i.putExtra("UserName", UserName);
                    // put image data in extras to send
                    i.putExtra("resID_uri", photo);
                    // put filename in extras
                    Log.d("chosen", chosen);
                    i.putExtra("chosen", chosen);
                    // put model type in extras
                    Log.d("quant", String.valueOf(quant));
                    i.putExtra("quant", quant);
                    // put filepath in extras
                    Log.d("imgPath", imgPath);
                    i.putExtra("imgPath", imgPath);
                    // send other required data
                    startActivity(i);
                }

            }
        }
    }

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    @Override
    public void onBackPressed() {
        // Default
        backPressHandler.onBackPressed();


    }


}
