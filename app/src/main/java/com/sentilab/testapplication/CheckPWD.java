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

public class CheckPWD extends AppCompatActivity {

    private String UserID, UserPassword;
    private EditText Password, ID;
    private TextView CheckBtn;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_p_w_d);

        // 아이디 비밀번호 EditText
        ID = findViewById(R.id.et_id);
        Password = findViewById(R.id.et_pass);

        // UserID 받는다.
        UserID = getIntent().getStringExtra("UserID");


        // 터치 불가능하게 만드는 함수
        ID.setFocusable(false);
        ID.setText(UserID);

        CheckBtn = findViewById(R.id.btn_check);
        CheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPassword = Password.getText().toString();
                new checkPWD().execute();
            }
        });

    }


    // 마이페이지용 비밀번호 확인
    public class checkPWD extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/UserLogin.php?userID=" + UserID;
            Log.d("Login userID", UserID);
            Log.d("Login userPassword", UserPassword);

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
                String pwd = jsonResponse.getString("userPassword");
                Log.d("LoginSuccess", success + "");


                if (!success) { // 정보가 존재하지 않을 때
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckPWD.this);
                    dialog = builder.setMessage("정보가 존재하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else { // 비밀번호가 존재할 때
                    if (pwd.equals(UserPassword)) { // 입력받은 비밀번호와 DB에 회원 비밀번호가 같을때
                        Intent intent = new Intent(CheckPWD.this, ChangeInfo.class);
                        intent.putExtra("UserID", UserID);
                        startActivity(intent);
                    } else { // 입력받은 비밀번호와 DB에 회원 비밀번호가 다를때
                        AlertDialog.Builder builder = new AlertDialog.Builder(CheckPWD.this);
                        dialog = builder.setMessage("현지 비밀번호가 일치하지 않습니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
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


