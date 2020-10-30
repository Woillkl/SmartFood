package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

public class PhotoAdminSendActivity extends AppCompatActivity {

    private String UserName, FilePath, foodname;
    private Bitmap bitmap;

    private ImageView selected_image;
    private EditText et_username, et_foodname;


    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_admin_send);

        selected_image = findViewById(R.id.selected_image);

        UserName = getIntent().getStringExtra("UserID");
        FilePath = getIntent().getStringExtra("filepath");

        et_username = findViewById(R.id.et_username);
        et_foodname = findViewById(R.id.et_foodname);

        bitmap = getIntent().getParcelableExtra("photo");
        selected_image.setImageBitmap(bitmap);

        et_username.setText(UserName);

        new android.app.AlertDialog.Builder(PhotoAdminSendActivity.this).setMessage("인공지능에게 도움이 되는 사진을 보내주세요!").create().show();

    }

    public void sendAdmin(View view) {
        foodname = et_foodname.getText().toString();
        if ("".equals(foodname)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PhotoAdminSendActivity.this);
            dialog = builder.setMessage("정보가 입력되지 않았습니다.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
            return;
        } else {

            //안드로이드에서 보낼 데이터를 받을 php 서버 주소
            String serverUrl = "http://graduateproject.dothome.co.kr/AdminPhotoSend.php";

            //Volley plus Library를 이용해서
            //파일 전송하도록..
            //Volley+는 AndroidStudio에서 검색이 안됨 [google 검색 이용]

            //파일 전송 요청 객체 생성[결과를 String으로 받음]
            SimpleMultiPartRequest smpr = new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PhotoAdminSendActivity.this);
                    builder.setTitle("SmartFood");
                    builder.setMessage("운영자에게 전송이 완료되었습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(PhotoAdminSendActivity.this, LoginMainActivity.class);
                                    intent.putExtra("ID", UserName);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    builder.show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(PhotoAdminSendActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            });

            //요청 객체에 보낼 데이터를 추가
            smpr.addStringParam("foodname", foodname);
            //이미지 파일 추가
            smpr.addFile("img", FilePath);

            //요청객체를 서버로 보낼 우체통 같은 객체 생성
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(smpr);
        }
    }


}
