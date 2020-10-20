package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;

public class ListDateActivity extends AppCompatActivity {

    ListView listView;
    DateAdapter dateAdapter;

    AlertDialog dialog;

    private String UserName;

    private String[] datas, rows;

    ArrayList<DateItem> dateItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_date);

        UserName = getIntent().getStringExtra("UserName");

        // 데이터를 서버에서 읽어오기
//        loadDB();
        new GetDate().execute();

        listView = findViewById(R.id.listView);

        dateAdapter = new DateAdapter(getLayoutInflater(), dateItems);
        listView.setAdapter(dateAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View curr = parent.getChildAt((int) id);
                TextView tv = curr.findViewById(R.id.tv_date);
                String date = tv.getText().toString();

                Intent intent = new Intent(ListDateActivity.this, GalleryActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("UserName", UserName);
                startActivity(intent);


            }
        });

    } // 메인 함수 끝

    void loadDB() {
        new Thread() {
            @Override
            public void run() {
                String serverUrl = "http://graduateproject.dothome.co.kr/GetDate.php?userID=" + UserName;

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
                        buffer.append(line + "\n");
                        line = reader.readLine();
                    }

                    String[] rows = buffer.toString().split(";");

                    dateItems.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dateAdapter.notifyDataSetChanged();
                        }
                    });

                    for (String row : rows) {
                        Log.d("%%%%%%%%%%%%%%ROW", row);

                        datas = row.split(";");

                        if (datas.length != 1) {
                            continue;
                        }

                        String date = datas[0];
                        Log.d("$$$$$$$$$$$$$$$$$$DATE", date);
                        dateItems.add(new DateItem(date));


                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public class GetDate extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/GetDate.php?userID=" + UserName;
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

//            if (UserName.equals("admin")) {
//                if (s.equals("Empty")) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ListDateActivity.this);
//                    builder.setTitle("SmartFood")
//                            .setMessage("수정해야 할 데이터가 없습니다.")
//                            .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent(ListDateActivity.this, LoginMainActivity.class);
//                                    intent.putExtra("ID", UserName);
//                                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
//                                    startActivity(intent);
//                                }
//                            })
//                            .create().show();
//                } else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ListDateActivity.this);
//                    dialog = builder.setMessage("수정해야 할 데이터들이 있습니다.")
//                            .setNegativeButton("확인", null)
//                            .create();
//                    dialog.show();
//                    // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
//                    String[] rows = s.split(";");
//
//                    // 대량의 데이터 초기화
//                    dateItems.clear();
//                    dateAdapter.notifyDataSetChanged();
//
//
//                    for (String row : rows) {
//                        // 한줄 데이터에서 한 칸씩 분리
//                        String[] datas = row.split(";");
//                        if (datas.length != 1) {
//                            continue;
//                        }
//
//                        String date = datas[0];
//
//
//                        dateItems.add(new DateItem(date));
//
//                        // 리스트뷰 갱신
//                        dateAdapter.notifyDataSetChanged();
//
//                    }
//                }
//
//            } else {
            if (s.equals("Empty")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListDateActivity.this);
                builder.setTitle("SmartFood")
                        .setMessage("저장된 사진이 없습니다.")
                        .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ListDateActivity.this, LoginMainActivity.class);
                                intent.putExtra("ID", UserName);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                                startActivity(intent);
                            }
                        })
                        .create().show();
            } else {
                // 읽어온 문자열에서 row(레코드)별로 분리하여 배열로 리턴하기
                rows = s.split(";");

                // 대량의 데이터 초기화
                dateItems.clear();
                dateAdapter.notifyDataSetChanged();


                for (String row : rows) {
                    // 한줄 데이터에서 한 칸씩 분리
                    String[] datas = row.split(";");
                    if (datas.length != 1) {
                        continue;
                    }

                    String date = datas[0];

                    // 대량의 데이터 ArrayList에 추가
                    dateItems.add(new DateItem(date));

                    // 리스트뷰 갱신
                    dateAdapter.notifyDataSetChanged();

                }
            }
//            }


        }


    }
}
