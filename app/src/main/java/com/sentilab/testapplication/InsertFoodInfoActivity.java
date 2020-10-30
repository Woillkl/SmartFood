package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InsertFoodInfoActivity extends AppCompatActivity {

    private String UserName, FoodName, FilePath;
    private Bitmap bitmap;

    private ImageView selected_image;
    private EditText et_username, et_foodname, et_time, et_amount, et_kcal;
    private Button btn_infosave;

    private String db_username, db_foodname, db_time, db_amount;
    private float db_get_kcal, db_kcal;

    private RadioGroup radioGroup;
    private RadioButton radioButton, radioBtn1;
    private String time;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_food_info);

        selected_image = findViewById(R.id.selected_image);
        et_username = findViewById(R.id.et_username);
        et_foodname = findViewById(R.id.et_foodname);
//        et_time = findViewById(R.id.et_time);
        et_amount = findViewById(R.id.et_amount);
        et_kcal = findViewById(R.id.et_kcal);

        radioGroup = findViewById(R.id.RadioGroup);
        radioBtn1 = findViewById(R.id.radio_btn1);

        UserName = getIntent().getStringExtra("UserName");
        FoodName = getIntent().getStringExtra("FoodName");
        FilePath = getIntent().getStringExtra("filepath");

        bitmap = getIntent().getParcelableExtra("photo");

        selected_image.setImageBitmap(bitmap);
        et_username.setText(UserName);
        et_foodname.setText(FoodName);


        new FoodInfoGet().execute();

        et_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_amount.getText().toString().length() == 0) {
                    et_kcal.setText("0");
                } else if (et_amount.getText().toString().length() != 0) {
                    db_kcal = Float.parseFloat(et_amount.getText().toString());
                    et_kcal.setText(db_kcal * db_get_kcal + " kcal");
                    db_kcal *= db_get_kcal;
                    Log.d("%%%%%%%%OnTextChanged", String.valueOf(db_kcal));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        db_time = radioBtn1.getText().toString();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton = findViewById(checkedId);
                db_time = radioButton.getText().toString();


            }
        });


    } // 메인 함수 끝

    // 음식 정보 찾기 웹통신
    public class FoodInfoGet extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/FoodFind.php?foodname=" + FoodName;
            Log.d("FoodFindFoodName", FoodName);

            try {
                Log.d("Background", "시작");
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String temp;//결과 값을 여기에 저장함

                StringBuilder stringBuilder = new StringBuilder();


                //버퍼생성후 한줄씩 가져옴
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp + "\n");
                    Log.d("결과값", temp + "");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                Log.d("stringBuilder", stringBuilder.toString().trim());
                return stringBuilder.toString().trim();//결과값이 여기에 리턴되면 이 값이 onPostExcute의 파라미터로 넘어감

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);


        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            try {
                Log.d("response", s);
                JSONObject jsonResponse = new JSONObject(s);
                boolean success = jsonResponse.getBoolean("success");
                Log.d("FoodFindsuccess", success + "");

                db_get_kcal = Float.parseFloat(jsonResponse.getString("kcal"));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


    public void BtnInfoInsert(View view) {


        db_username = et_username.getText().toString();
        db_foodname = et_foodname.getText().toString();
        Log.d("%%%%%%%%%%%%%%%%%%%%", db_time);
        db_amount = et_amount.getText().toString();


        if ("".equals(db_foodname) || "".equals(db_time) || "".equals(db_amount)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(InsertFoodInfoActivity.this);
            dialog = builder.setMessage("모든 정보를 입력해주세요.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
        } else {

            //안드로이드에서 보낼 데이터를 받을 php 서버 주소
            String serverUrl = "http://graduateproject.dothome.co.kr/UploadPhoto.php";

            //Volley plus Library를 이용해서
            //파일 전송하도록..
            //Volley+는 AndroidStudio에서 검색이 안됨 [google 검색 이용]

            //파일 전송 요청 객체 생성[결과를 String으로 받음]
            SimpleMultiPartRequest smpr = new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InsertFoodInfoActivity.this);
                    builder.setTitle("SmartFood");
                    builder.setMessage("사진 저장이 완료되었습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(InsertFoodInfoActivity.this, LoginMainActivity.class);
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
                    Toast.makeText(InsertFoodInfoActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            });

            //요청 객체에 보낼 데이터를 추가
            smpr.addStringParam("userid", db_username);
            smpr.addStringParam("foodname", db_foodname);
            smpr.addStringParam("time", db_time);
            smpr.addStringParam("amount", db_amount);
            smpr.addStringParam("kcal", String.valueOf(db_kcal));

            //이미지 파일 추가
            smpr.addFile("img", FilePath);

            //요청객체를 서버로 보낼 우체통 같은 객체 생성
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(smpr);
        }
    }

}
