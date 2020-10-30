package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyPageActivity extends AppCompatActivity {

    private String UserName;
    private TextView txt_userID, txt_userPassword, txt_userName, txt_userPhonenumber, ChangeInfoBtn, WithdrawalBtn;
    private String userID, userPassword, userName, userPhonenumber;

    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        // getIntent로 사용자의 아이디를 받아온다.
        UserName = getIntent().getStringExtra("UserName");
        Log.d("@@@@@@@@@@LoginuserName", UserName);


        // 로그아웃 버튼 클릭 시
        final TextView logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                builder.setMessage("정말 로그아웃 하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
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


        // 회원탈퇴 버튼 클릭
        WithdrawalBtn = findViewById(R.id.withdrawal);
        WithdrawalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UserName.equals("admin")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                    dialog = builder.setMessage("관리자는 회원탈퇴 불가.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else {
                    Intent intent = new Intent(MyPageActivity.this, WithdrawalActivity.class);
                    intent.putExtra("UserName", UserName);
                    startActivity(intent);
                }

            }
        });

        Log.d("MyPageAsync", "시작");
        new MyPage().execute();
    }// 메인페이지 끝

    // 마이페이지용 웹 통신
    public class MyPage extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/MyPage.php?userID=" + UserName;
            Log.d("Mypage userID ", UserName);

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

                // 회원정보를 가져온다.
                userID = jsonResponse.getString("userID");
                userPassword = jsonResponse.getString("userPassword");
                userName = jsonResponse.getString("userName");
                userPhonenumber = jsonResponse.getString("userPhonenumber");

                // 위젯 참조
                txt_userID = findViewById(R.id.mypage_id);
                txt_userPassword = findViewById(R.id.mypage_password);
                txt_userName = findViewById(R.id.mypage_name);
                txt_userPhonenumber = findViewById(R.id.mypage_phonenumber);

                txt_userID.setText(userID);
                txt_userName.setText(userName);
                txt_userPhonenumber.setText(userPhonenumber);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 회원정보 변경 Click
            ChangeInfoBtn = findViewById(R.id.changeinfo);
            ChangeInfoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (UserName.equals("admin")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                        dialog = builder.setMessage("관리자는 정보 변경 불가.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    } else {
                        Intent intent = new Intent(MyPageActivity.this, CheckPWD.class);
                        intent.putExtra("UserID", userID);
                        startActivity(intent);
                    }

                }
            });

        }


    }
}
