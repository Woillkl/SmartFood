package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private GridView GridView;
    GalleryAdapter galleryAdapter;
    private String username, date;
    private AlertDialog dialog;
    private ListView listView;
    private TextView tv_totalkcal;
    private float totalkcal;

    ArrayList<GalleryItem> galleryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        username = getIntent().getStringExtra("UserName");
        Log.d("Gallery USERNAME", username);
        date = getIntent().getStringExtra("date");
        // 데이터를 서버에서 읽어오기
//        GetGalleryData();

        new GetGalleryData().execute();

        listView = findViewById(R.id.listView);
        galleryAdapter = new GalleryAdapter(getLayoutInflater(), galleryItems);
        listView.setAdapter(galleryAdapter);
        tv_totalkcal = findViewById(R.id.tv_totalkcal);

    }

    void GetGalleryData() {
        // volley library로 사용 가능
        // 전통적 기법

        new Thread() {
            @Override
            public void run() {

                String serverUrl = "http://graduateproject.dothome.co.kr/Gallery.php?userID=" + username + "&date=" + date;
                Log.d("@@@@@UserName", username);

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr);

                    final StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();
                    while (line != null) {
                        buffer.append(line + "/n");
                        Log.d("결과값", line + "");

                        line = reader.readLine();
                    }


                    // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
                    String[] rows = buffer.toString().split(";");

                    // 대량의 데이터 초기화
                    galleryItems.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            galleryAdapter.notifyDataSetChanged();
                        }
                    });

                    for (String row : rows) {
                        // 한줄 데이터에서 한 칸씩 분리
                        String[] datas = row.split("&");
                        Log.d("%%%%%%%%%%%%%%ROW", row);
                        if (datas.length != 5) {
                            continue;
                        }

                        int bno = Integer.parseInt(datas[0]);
                        String FoodName = datas[1];
                        String imgPath = "http://graduateproject.dothome.co.kr/" + datas[2];
                        String time = datas[3];
                        String kcal = datas[4];
                        String date = datas[5];


                        // 대량의 데이터 ArrayList에 추가
                        galleryItems.add(new GalleryItem(bno, FoodName, imgPath, time, kcal, date));

                        // 리스트뷰 갱신
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                galleryAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public class GetGalleryData extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/Gallery.php?userID=" + username + "&date=" + date;
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

            if (username.equals("admin")) {
                if (s.equals("Empty")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    builder.setTitle("SmartFood")
                            .setMessage("수정해야 할 데이터가 없습니다.")
                            .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(GalleryActivity.this, LoginMainActivity.class);
                                    intent.putExtra("ID", username);
                                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                    startActivity(intent);
                                }
                            })
                            .create().show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    dialog = builder.setMessage("수정해야 할 데이터들이 있습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
                    String[] rows = s.split(";");

                    // 대량의 데이터 초기화
                    galleryItems.clear();
                    galleryAdapter.notifyDataSetChanged();


                    for (String row : rows) {
                        // 한줄 데이터에서 한 칸씩 분리
                        String[] datas = row.split("&");
                        if (datas.length != 7) {
                            Log.d("@@@@@@@@","OhMyGod");
                            continue;
                        }

                        int bno = Integer.parseInt(datas[0]);
                        Log.d("$$$$$$$$$$$$BNO",String.valueOf(bno));
                        String FoodName = datas[2];
                        String imgPath = "http://graduateproject.dothome.co.kr/" + datas[3];
                        String time = datas[4];
                        String kcal = datas[5];
                        String date = datas[6];

                        Log.d("#*#*#*#imgPath", imgPath);
                        // 대량의 데이터 ArrayList에 추가
                        galleryItems.add(new GalleryItem(bno, FoodName, imgPath, time, kcal, date));

                        // 리스트뷰 갱신
                        galleryAdapter.notifyDataSetChanged();

                    }
                }

            } else {
                if (s.equals("Empty")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    builder.setTitle("SmartFood")
                            .setMessage("저장된 사진이 없습니다.")
                            .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(GalleryActivity.this, LoginMainActivity.class);
                                    intent.putExtra("ID", username);
                                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                    startActivity(intent);
                                }
                            })
                            .create().show();
                } else {
                    // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
                    String[] rows = s.split(";");

                    // 대량의 데이터 초기화
                    galleryItems.clear();
                    galleryAdapter.notifyDataSetChanged();


                    for (String row : rows) {
                        // 한줄 데이터에서 한 칸씩 분리
                        String[] datas = row.split("&");
                        if (datas.length != 7) {
                            Log.d("@@@@@@@@","OhMyGod");
                            continue;
                        }

                        int bno = Integer.parseInt(datas[0]);
                        Log.d("$$$$$$$$$$$$BNO",String.valueOf(bno));
                        String FoodName = datas[2];
                        String imgPath = "http://graduateproject.dothome.co.kr/" + datas[3];
                        String time = datas[4];
                        String kcal = datas[5];
                        String date = datas[6];

                        totalkcal += Float.parseFloat(kcal);

                        Log.d("#*#*#*#imgPath", imgPath);
                        // 대량의 데이터 ArrayList에 추가
                        galleryItems.add(new GalleryItem(bno, FoodName, imgPath, time, kcal, date));

                        // 리스트뷰 갱신
                        galleryAdapter.notifyDataSetChanged();

                    }

                    tv_totalkcal.setText(totalkcal + "kcal");
                }
            }


        }


    }




}
