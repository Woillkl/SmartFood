package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WithdrawalActivity extends AppCompatActivity {

    private String UserName, AsyncPassword, Password;
    private Button WithDrawalBtn;
    private EditText et_password;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);

        et_password = findViewById(R.id.et_pass);
        UserName = getIntent().getStringExtra("UserName");

        Log.d("MyPageAsync", "시작");
        new MyPage().execute();

    }

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
                AsyncPassword = jsonResponse.getString("userPassword");
                Log.d("AsyncPassword", AsyncPassword);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // 회원정보 변경 Click
            WithDrawalBtn = findViewById(R.id.withdrawalbtn);


            WithDrawalBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Password = et_password.getText().toString();

                    if (!Password.equals(AsyncPassword)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawalActivity.this);
                        dialog = builder.setMessage("비밀번호가 일치하지 않습니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(WithdrawalActivity.this);
                        dialog.setTitle("SmartFood")
                                .setMessage("정말 회원 탈퇴를 진행하시겠습니까?")
                                .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Withdraw().execute();
                                    }
                                })
                                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create().show();
                    }

                }
            });

        }


    }


    // 회원탈퇴용 웹 통신
    public class Withdraw extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.


            Log.d("WithDraw 실행", "WithDraw 실행");
            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/WithDrawal.php?userID=" + UserName;
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
                Log.d("회원탈퇴 버튼 클릭 후", stringBuilder.toString().trim());
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

                if (success) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawalActivity.this);
                    builder.setTitle("SmartFood");
                    builder.setMessage("회원탈퇴를 완료하셨습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(WithdrawalActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                    startActivity(intent);

                                    finish();
                                }
                            });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawalActivity.this);
                    dialog = builder.setMessage("회원탈퇴에 실패하셨습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();

                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }
}
