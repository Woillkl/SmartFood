package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AdminGalleryActivity extends AppCompatActivity {

    private String foodname, UserName;
    private GridView gridView;

    AdminGalleryAdapter adminGalleryAdapter;
    private AlertDialog dialog;

    private TextView tv_title;
    private ImageView iv;

    ArrayList<AdminGalleryItem> adminGalleryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_gallery);

        foodname = getIntent().getStringExtra("foodname");
        Log.d("AdminGallery의 FoodName",foodname);
        UserName = getIntent().getStringExtra("UserName");

        tv_title = findViewById(R.id.tv_title);

        tv_title.setText(foodname);

        new GetFoodData().execute();

        gridView = findViewById(R.id.gridView);
        adminGalleryAdapter = new AdminGalleryAdapter(getLayoutInflater(), adminGalleryItems);
        gridView.setAdapter(adminGalleryAdapter);
    } // 메인 함수 끝


    public class GetFoodData extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/AdminGallery.php?userID=" + UserName + "&photoname=" + foodname;
            Log.d("##Async시작##", "시작!!!!");

            try {
                Log.d("Background", "시작");
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String temp;//결과 값을 여기에 저장함

                StringBuffer buffer = new StringBuffer();


                //버퍼생성후 한줄씩 가져옴
                while ((temp = bufferedReader.readLine()) != null) {
                    buffer.append(temp + "\n");
                    Log.d("결과값", temp + "");
                }


                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                Log.d("stringBuilder", buffer.toString().trim());
                return buffer.toString().trim();//결과값이 여기에 리턴되면 이 값이 onPostExcute의 파라미터로 넘어감

            } catch (MalformedURLException e) {
                e.printStackTrace();
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

            Log.d("##@$@$$@$@#S의 결과값", s);


            // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
            String[] rows = s.split(";");

            // 대량의 데이터 초기화
            adminGalleryItems.clear();
            adminGalleryAdapter.notifyDataSetChanged();


            for (String row : rows) {
                // 한줄 데이터에서 한 칸씩 분리
                String[] datas = row.split(";");
                if (datas.length != 1) {
                    Log.d("@@@@@@@@", "OhMyGod");
                    continue;
                }


                String imgPath = "http://graduateproject.dothome.co.kr/" + datas[0];


                Log.d("#*#*#*#imgPath", imgPath);
                // 대량의 데이터 ArrayList에 추가
                adminGalleryItems.add(new AdminGalleryItem(imgPath));

                // 리스트뷰 갱신
                adminGalleryAdapter.notifyDataSetChanged();

            }
        }


    }


}




