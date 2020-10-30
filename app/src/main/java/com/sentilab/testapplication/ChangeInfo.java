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
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangeInfo extends AppCompatActivity {

    private AlertDialog dialog;

    private TextView userid;
    private String UserName, UserPhone, UserID;
    private EditText name, phone;

    private Button BtnPWDChange;
    private TextView BtnInfoChange;

    // 새로운 비밀번호 변경 변수
    private EditText userpwd, newpwd1, newpwd2;
    private String UserPassword, PWD, NewPWD1, NewPWD2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);

        // 기본정보 위젯 참조
        userid = findViewById(R.id.id);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phonenumber);
        BtnInfoChange = findViewById(R.id.changeinfo);

        UserID = getIntent().getStringExtra("UserID");
        new Member().execute();

        // 정보 변경 버튼 클릭
        BtnInfoChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserID = userid.getText().toString();
                UserName = name.getText().toString();
                UserPhone = phone.getText().toString();

                new ChangeUserInfo().execute();

            }
        });


        // 비밀번호 관련 위젯
        userpwd = findViewById(R.id.presentpwd);
        newpwd1 = findViewById(R.id.newpwd1);
        newpwd2 = findViewById(R.id.newpwd2);
        BtnPWDChange = findViewById(R.id.changepwd);

        // 비밀번호 변경 버튼 클릭
        BtnPWDChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PWD = userpwd.getText().toString();
                NewPWD1 = newpwd1.getText().toString();
                NewPWD2 = newpwd2.getText().toString();

                if (!PWD.equals(UserPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);
                    dialog = builder.setMessage("현재 비밀번호가 일치하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else {
                    if (PWD.equals(NewPWD1)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);
                        dialog = builder.setMessage("현재 비밀번호와 바꾸실 비밀번호가 일치합니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    } else {
                        if (!NewPWD1.equals(NewPWD2)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);
                            dialog = builder.setMessage("바꾸실 비밀번호가 일치하지 않습니다.")
                                    .setNegativeButton("확인", null)
                                    .create();
                            dialog.show();
                        } else {
                            new ChangePWD().execute();
                        }
                    }
                }


            }
        });
    }


    // 정보 가져오기 ASYNC
    public class Member extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/MyPage.php?userID=" + UserID;
            Log.d("Login userID", UserID);

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
                Log.d("LoginSuccess", success + "");

                UserID = jsonResponse.getString("userID");
                UserName = jsonResponse.getString("userName");
                UserPhone = jsonResponse.getString("userPhonenumber");
                UserPassword = jsonResponse.getString("userPassword");

                userid.setText(UserID);
                name.setText(UserName);
                phone.setText(UserPhone);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


    // 회원정보 변경 웹통신
    public class ChangeUserInfo extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.


            String target = "http://graduateproject.dothome.co.kr/UserInfoChange.php?userID=" + UserID +
                    "&userName=" + UserName + "&userPhonenumber=" + UserPhone;

            try {
                Log.d("RegisterBackground", "시작");
                URL url = new URL(target);
                Log.d("Target", target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("1", "1");

                InputStream inputStream = httpURLConnection.getInputStream();
                Log.d("2", "2");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String result;//결과 값을 여기에 저장함

                StringBuilder stringBuilder = new StringBuilder();

                //버퍼생성후 한줄씩 가져옴
                while ((result = bufferedReader.readLine()) != null) {
                    stringBuilder.append(result + "\n");
                    Log.d("결과값", result + "");
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
                Button validateButton = findViewById(R.id.btn_validate);
                Log.d("response", s);
                JSONObject jsonResponse = new JSONObject(s);
                boolean success = jsonResponse.getBoolean("success");
                if (success) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);

                    builder.setMessage("회원정보 변경을 성공하셨습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(ChangeInfo.this, MyPageActivity.class);
                                    intent.putExtra("UserName", UserID);

                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정

                                    startActivity(intent);


                                    finish();
                                }
                            });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);
                    dialog = builder.setMessage("변경에 실패하셨습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }


    // 비밀번호 변경 웹통신
    public class ChangePWD extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.


            String target = "http://graduateproject.dothome.co.kr/ChangePassword.php?userID=" + UserID +
                    "&userPassword=" + NewPWD2;
            Log.d("NEWPWD2", NewPWD2);

            try {
                Log.d("RegisterBackground", "시작");
                URL url = new URL(target);
                Log.d("Target", target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("1", "1");

                InputStream inputStream = httpURLConnection.getInputStream();
                Log.d("2", "2");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String result;//결과 값을 여기에 저장함

                StringBuilder stringBuilder = new StringBuilder();

                //버퍼생성후 한줄씩 가져옴
                while ((result = bufferedReader.readLine()) != null) {
                    stringBuilder.append(result + "\n");
                    Log.d("결과값", result + "");
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
                Button validateButton = findViewById(R.id.btn_validate);
                Log.d("response", s);
                JSONObject jsonResponse = new JSONObject(s);
                boolean success = jsonResponse.getBoolean("success");
                if (success) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);

                    builder.setMessage("비밀번호가 변경되었습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(ChangeInfo.this, LoginMainActivity.class);
                                    intent.putExtra("ID", UserID);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                    startActivity(intent);

                                    finish();
                                }
                            });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfo.this);
                    dialog = builder.setMessage("변경에 실패하셨습니다.")
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
