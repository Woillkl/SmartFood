package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private String userID, userPassword, checkPassword, userName, userPhonenumber;
    private EditText et_userID, et_userPassword, et_checkPassword, et_userName, et_userPhonenumber;
    private AlertDialog dialog;

    private boolean validate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        //위젯에 대한 참조.
        et_userID = findViewById(R.id.et_id);
        et_userPassword = findViewById(R.id.et_pass);
        et_checkPassword = findViewById(R.id.et_pass1);
        et_userName = findViewById(R.id.et_name);
        et_userPhonenumber = findViewById(R.id.et_phonenumber);


        // 아이디 중복 확인
        final Button validateButton = findViewById(R.id.btn_validate);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = et_userID.getText().toString();
                // 중복체크 됐을때
                if (validate) {
                    return;
                }
                // 아이디 빈칸일때
                if (userID.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디는 빈 칸일 수 없습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                Log.d("ValidateAsync", "시작");
                // Validate Async 시작
                new Validate().execute();


            }
        });



        // 회원가입
        final Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("회원가입button","시작");
                userID = et_userID.getText().toString();
                userPassword = et_userPassword.getText().toString();
                checkPassword = et_checkPassword.getText().toString();
                userName = et_userName.getText().toString();
                userPhonenumber = et_userPhonenumber.getText().toString();
                Log.d("userID",userID);

                // 중복 확인을 하지 않았을 때
                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("먼저 중복 체크를 해주세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                // 정보가 입력되지 않았을 때
                if (userID.equals("") || userPassword.equals("") || checkPassword.equals("") || userName.equals("") || userPhonenumber.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("정보가 입력되지 않았습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                // 비밀번호가 일치하지 않았을 때
                if (!userPassword.equals(checkPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호가 일치하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                if (userPhonenumber.length() != 11) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("올바른 전화번호를 입력하세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                Log.d("RegisterAsync", "시작");
                // Register Async 시작
                new Register().execute();

            }
        });


    } // onCreate끝


    // 아이디 중복 확인용 웹 통신
    public class Validate extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/UserValidate.php?userID=" + userID;
            Log.d("validate userID",userID);
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

                // 중복된 값이 없을때
                if (success) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("사용 가능한 아이디");
                    builder.setMessage("사용 하시겠습니까?");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Button validateButton = findViewById(R.id.btn_validate);

                                    et_userID.setEnabled(false);
                                    validateButton.setEnabled(false);
                                    validate = true;
                                    validateButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                    builder.show();

                } else { // 중복된 값이 있을때
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("사용할 수 없는 아이디입니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }



    // 회원가입용 웹통신
    public class Register extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.


            String target = "http://graduateproject.dothome.co.kr/UserRegister.php?userID=" + userID + "&userPassword=" + userPassword +
                    "&userName=" + userName + "&userPhonenumber=" + userPhonenumber;

            try {
                Log.d("RegisterBackground", "시작");
                URL url = new URL(target);
                Log.d("Target",target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("1","1");

                InputStream inputStream = httpURLConnection.getInputStream();
                Log.d("2","2");

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

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                    builder.setMessage("회원가입을 성공하셨습니다.");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //기존에 쌓여있던 스택을 모두 없앤다.
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // task를 새로 생성한다

                                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정

                                    startActivity(intent);


                                    finish();
                                }
                            });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("회원 등록에 실패하셨습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();

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
