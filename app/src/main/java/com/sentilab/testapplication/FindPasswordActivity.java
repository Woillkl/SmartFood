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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FindPasswordActivity extends AppCompatActivity {

    private String userPhonenumber, userID;
    private EditText et_userPhonenumber, et_userID;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);


        //위젯에 대한 참조.
        et_userPhonenumber = findViewById(R.id.et_phonenumber);
        et_userID = findViewById(R.id.et_id);

        // 아이디 찾기 버튼
        final Button findpasswordButton = findViewById(R.id.btn_findpassword);
        findpasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 값을 string으로 받아온다.
                userID = et_userID.getText().toString();
                userPhonenumber = et_userPhonenumber.getText().toString();

                // 정보가 입력되지 않았을 때
                if (userID.equals("") || userPhonenumber.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindPasswordActivity.this);
                    dialog = builder.setMessage("정보가 입력되지 않았습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                Log.d("FindPW", "시작");
                // Login Async 시작
                new FindPW().execute();
            }
        });


    } // onCreate끝


    // 아이디 찾기용 웹 통신
    public class FindPW extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/UserFindPW.php?userID=" + userID + "&userPhonenumber=" + userPhonenumber;
            Log.d("FindPW userID", userID);
            Log.d("FindPW userPhonenumber", userPhonenumber);

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
                Log.d("FindPassword", success + "");

                if (!success) { // 아이디가 없을 때
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindPasswordActivity.this);
                    dialog = builder.setMessage("정보가 일치하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else { // 아이디가 있을 때

                    String checkPassword = jsonResponse.getString("userPassword");
                    Log.d("FindPassword", checkPassword);

                    AlertDialog.Builder builder = new AlertDialog.Builder(FindPasswordActivity.this);
                    builder.setTitle("P/W 찾기");
                    builder.setMessage("비밀번호 : " + checkPassword);
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(FindPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    builder.show();



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
