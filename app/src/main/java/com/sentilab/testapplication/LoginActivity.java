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

public class LoginActivity extends AppCompatActivity {

    private String userID, userPassword;
    private EditText et_userID, et_userPassword;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //위젯에 대한 참조.
        et_userID = findViewById(R.id.et_id);
        et_userPassword = findViewById(R.id.et_pass);

        // 회원가입 버튼(TextView)
        TextView registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // ID찾기 버튼(TextView)
        TextView findidButton = findViewById(R.id.btn_findid);
        findidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FindIDActivity.class);
                startActivity(intent);
            }
        });

        // P/W 찾기 버튼(TextView)
        TextView findpasswordButton = findViewById(R.id.btn_findpassword);
        findpasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼 입력
        final Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = et_userID.getText().toString();
                userPassword = et_userPassword.getText().toString();

                // 정보가 입력되지 않았을 때
                if (userID.equals("") || userPassword.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    dialog = builder.setMessage("정보가 입력되지 않았습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                Log.d("LoginAsync", "시작");
                // Login Async 시작
                new Login().execute();


            }
        });
    } // onCreate끝


    // 아이디 중복 확인용 웹 통신
    public class Login extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/UserLogin.php?userID=" + userID + "&userPassword=" + userPassword;
            Log.d("Login userID", userID);
            Log.d("Login userPassword", userPassword);

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


                if (!success) { // 아이디가 없을 때
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    dialog = builder.setMessage("아이디가 존재하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else { // 아이디가 있을 때

                    final String checkID = jsonResponse.getString("userID");
                    String checkPassword = jsonResponse.getString("userPassword");

                    Log.d("LogincheckID", checkID);
                    Log.d("LogincheckPassword", checkPassword);

                    // 비밀번호가 틀렸을 때
                    if (!checkPassword.equals(userPassword)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("비밀번호를 확인해 주세요.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    } else { // 모든 정보가 일치할 때

                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("로그인에 성공했습니다.");
                        builder.setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(LoginActivity.this, LoginMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //기존에 쌓여있던 스택을 모두 없앤다.
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // task를 새로 생성한다
                                        if (checkID.equals("admin")) {
                                            intent.putExtra("ID", "Administrator");
                                        } else {
                                            intent.putExtra("ID", checkID);
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        builder.show();

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
