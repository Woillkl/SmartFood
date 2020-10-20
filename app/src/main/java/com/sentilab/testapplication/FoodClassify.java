package com.sentilab.testapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;


public class FoodClassify extends AppCompatActivity {

    // presets for rgb conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private String foodname;
    private String foodinfo;
    private String averagecal;

    // 이미지 경로 String
    private String imgPath;

    // 넘겨받은 사용자 아이디
    private String UserName;

    // 사진 Bitmap url
    private Bitmap bitmap;
    private Bitmap resizebitmap;
    // options for model interpreter
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    // tflite graph
    private Interpreter tflite;
    // holds all the possible labels for model
    private List<String> labelList;
    // holds the selected image data as bytes
    private ByteBuffer imgData = null;
    // holds the probabilities of each label for non-quantized graphs
    private float[][] labelProbArray = null;
    // holds the probabilities of each label for quantized graphs
    private byte [][] labelProbArrayB = null;
    // array tha holds the labels with the highest probabilities
    private String[] topLables = null;
    // array that holds the highest probabilities
    private String[] topConfidence = null;
    // float value array for topConfidence
    private Float[] floatConfidence = null;

    Random random = new Random();

    // selected classifier information received from extras
    private String chosen;
    private boolean quant;

    // input image dimensions for the Inception Model
    private int DIM_IMG_SIZE_X = 224;
    private int DIM_IMG_SIZE_Y = 224;
    private int DIM_PIXEL_SIZE = 3;

    // int array to hold image data
    private int[] intValues;

    // 로그인이 됐는지 안됐는지 전 intent에서 받는다.
    private String logined;

    // activity elements
    private ImageView selected_image;
    private Button classify_button;
    private Button back_button;
    private TextView label1;
    private TextView label2;
    private TextView label3;
    private TextView Confidence1;
    private TextView Confidence2;
    private TextView Confidence3;
    private TextView FinalText, loginplease;
    private Button upload_btn, notfound_btn;


    private TextView FoodInfo;
    private TextView AverageCal;

    AlertDialog dialog;

    // priority queue that will hold the top results from the CNN
    private PriorityQueue<Map.Entry<String,Float>> sortedLabels =
            new PriorityQueue<>(RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get all selected classifier data from classifiers
        chosen = getIntent().getStringExtra("chosen");
        quant = getIntent().getBooleanExtra("quant", false);


        // 로그인을 했는지 확인
        logined = getIntent().getStringExtra("logined");
        Log.d("!@#$%^#%#%Logined",logined);




        // initialize array that holds image data
        intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        // 메인 함수
        super.onCreate(savedInstanceState);

        // initialize graph and labels
        try {
            tflite = new Interpreter(loadModelFile(), tfliteOptions);
            labelList = loadLabelList();
            Log.d("********TFLITE",String.valueOf(tflite));
            Log.d("***LABELLIST******",String.valueOf(labelList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 로그인 됐으면 유저 아이디 가져온다.
        if(logined.equals("yes")) {
            UserName = getIntent().getStringExtra("UserName");
            Log.d("FOODCLASSIFY의 USERNAME",UserName);
            imgPath = getIntent().getStringExtra("imgPath");
            Log.d("FOODCLASSIFY의 IMGPATH",imgPath);
        }

        // initialize byte array. The size depends if the input data needs to be quantized or not
        if (quant) {
            imgData =
                    ByteBuffer.allocateDirect(
                            DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        } else {
            imgData =
                    ByteBuffer.allocateDirect(
                            4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        }
        Log.d("**********IMAGEDATA",String.valueOf(imgData));
        imgData.order(ByteOrder.nativeOrder());

        // initialize probabilities array. The datatypes that array holds depends if the input data needs to be quantized or not
        if (quant) {
            labelProbArrayB = new byte[1][labelList.size()];
        } else {
            labelProbArray = new float[1][labelList.size()];
        }
        Log.d("*********LabelProbArray",labelProbArray + " *labelProbArrayB* " + labelProbArrayB);
        Log.d("(*)()()()()(quant",String.valueOf(quant));
        // 메인 함수
        setContentView(R.layout.activity_food_classify);


        // not sure why this happens, but without this the image appears on its side

        // labels that hold top three results of CNN
//        label1 = (TextView) findViewById(R.id.label1);
//        label2 = (TextView) findViewById(R.id.label2);
//        label3 = (TextView) findViewById(R.id.label3);
        // displays the probabilities of top labels
//        Confidence1 = (TextView) findViewById(R.id.Confidence1);
//        Confidence2 = (TextView) findViewById(R.id.Confidence2);
//        Confidence3 = (TextView) findViewById(R.id.Confidence3);

        FinalText = findViewById(R.id.final_text);
        // initialize imageView that displays selected image to the user
        selected_image = findViewById(R.id.selected_image);

        // get image from previous activity to show in the imageView
        bitmap = getIntent().getParcelableExtra("resID_uri");
        selected_image.setImageBitmap(bitmap);

        // initialize array to hold top labels
        topLables = new String[RESULTS_TO_SHOW];
        Log.d("#####topLABLES",String.valueOf(topLables));
        // initialize array to hold top probabilities
        topConfidence = new String[RESULTS_TO_SHOW];
        Log.d("#####topConfidence",String.valueOf(topConfidence));
        // float array for store float confidence
        floatConfidence = new Float[RESULTS_TO_SHOW];



        // classify current displayed image
        classify_button = findViewById(R.id.classify_image);
        upload_btn = findViewById(R.id.upload_Button);
        notfound_btn = findViewById(R.id.notfoundfood);
        loginplease = findViewById(R.id.loginplease);
        classify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(logined.equals("yes")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FoodClassify.this);
                    dialog = builder.setMessage("음식의 정보가 틀리면 운영자에게 보내주세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                }

                // get current bitmap from imageView
                Bitmap bitmap_orig = ((BitmapDrawable) selected_image.getDrawable()).getBitmap();

                // resize the bitmap to the required input size to the CNN
                resizebitmap = getResizedBitmap(bitmap_orig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
                // convert bitmap to byte array
                convertBitmapToByteBuffer(resizebitmap);
                // pass byte data to the graph
                Log.d("&&LABELPROBQUANT",String.valueOf(quant));
                if (quant) {
                    tflite.run(imgData, labelProbArrayB);
                } else {
                    tflite.run(imgData, labelProbArray);
                }
                // display the results
                printTopKLabels();

                if(floatConfidence[2] >= 90) {
                    if(logined.equals("yes") && !UserName.equals("Administrator")) {
                        loginplease.setVisibility(View.GONE);
                        upload_btn.setVisibility(View.VISIBLE);
                        notfound_btn.setVisibility(View.VISIBLE);
                    } else if(logined.equals("no")) {
                        upload_btn.setVisibility(View.GONE);
                        notfound_btn.setVisibility(View.GONE);
                        loginplease.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (logined.equals("yes")) {
                        loginplease.setVisibility(View.GONE);
                        notfound_btn.setVisibility(View.VISIBLE);
                    } else if(logined.equals("no")) {
                        upload_btn.setVisibility(View.GONE);
                        notfound_btn.setVisibility(View.GONE);
                        loginplease.setVisibility(View.VISIBLE);
                    }
                }




            }
        });


        Log.d("@#$@#$@#LOGINED",logined);
        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (logined.equals("no")) {
                    Intent i = new Intent(FoodClassify.this, MainActivity.class);
                    i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                    startActivity(i);
                } else if(logined.equals("yes")) {
                    Intent i = new Intent(FoodClassify.this, LoginMainActivity.class);
                    i.putExtra("ID",UserName);
                    i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                    startActivity(i);
                }
            }
        });

    } // 메인 함수 끝



    // loads tflite grapg from file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(chosen);
        Log.d("여기기기기근","AssetFileDescriptor 아래");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    // converts bitmap to byte array which is passed in the tflite graph
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float
                if(quant){
                    imgData.put((byte) ((val >> 16) & 0xFF));
                    imgData.put((byte) ((val >> 8) & 0xFF));
                    imgData.put((byte) (val & 0xFF));
                } else {
                    imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        }
    }

    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        Log.d("Label", "라벨 열었따.");
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }


    // print the top labels and respective confidences
    private void printTopKLabels() {
        // add all results to priority queue
        for (int i = 0; i < labelList.size(); ++i) {
            if(quant){
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), (labelProbArrayB[0][i] & 0xff) / 255.0f));
            } else {
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            }
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        // get top results from priority queue
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            topLables[i] = label.getKey();
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
            Log.d("^&^&^&^&Label",String.format("%.0f",label.getValue()*100));
            floatConfidence[i] = Float.parseFloat(String.format("%f", label.getValue() * 100));
        }
        Log.d("WEFSEFSDFE",String.valueOf(floatConfidence));

        // set the corresponding textviews with the results
//        label1.setText("1. "+topLables[2]);
//        label2.setText("2. "+topLables[1]);
//        label3.setText("3. "+topLables[0]);
//        Confidence1.setText(topConfidence[2]);
//        Confidence2.setText(topConfidence[1]);
//        Confidence3.setText(topConfidence[0]);

        // display bottom Text
        if(floatConfidence[2] >= 90) {
            FinalText.setText(topLables[2]);
            foodname = topLables[2];
            new FindFood().execute();

        } else {
            FinalText.setText("그런 정보가 없습니다.");
        }
    }

    // 사전 저장 버튼으로 사진 서버로 보내기
    public void clickUpload(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(FoodClassify.this);
        builder.setMessage("정보를 입력해주세요.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(FoodClassify.this, InsertFoodInfoActivity.class);
                        intent.putExtra("UserName",UserName);
                        intent.putExtra("FoodName",foodname);
                        intent.putExtra("filepath",imgPath);
                        intent.putExtra("photo",bitmap);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();

//        //안드로이드에서 보낼 데이터를 받을 php 서버 주소
//        String serverUrl="http://graduateproject.dothome.co.kr/UploadPhoto.php";
//
//        //Volley plus Library를 이용해서
//        //파일 전송하도록..
//        //Volley+는 AndroidStudio에서 검색이 안됨 [google 검색 이용]
//
//        //파일 전송 요청 객체 생성[결과를 String으로 받음]
//        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(FoodClassify.this);
//                builder.setTitle("SmartFood");
//                builder.setMessage("사진 저장이 완료되었습니다.");
//                builder.setPositiveButton("확인",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent(FoodClassify.this, LoginMainActivity.class);
//                                intent.putExtra("ID",UserName);
//                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
//                                startActivity(intent);
//                                finish();
//                            }
//                        });
//                builder.show();
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(FoodClassify.this, "ERROR", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        //요청 객체에 보낼 데이터를 추가
//        smpr.addStringParam("userid", UserName);
//        smpr.addStringParam("foodname", foodname);
//        //이미지 파일 추가
//        smpr.addFile("img", imgPath);
//
//        //요청객체를 서버로 보낼 우체통 같은 객체 생성
//        RequestQueue requestQueue= Volley.newRequestQueue(this);
//        requestQueue.add(smpr);

    }

    // 관리자에게 결과 없는 사진 보내기
    public void NotFoundPhoto(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(FoodClassify.this);
        dialog  .setTitle("사진 전송")
                .setMessage("운영자에게 사진을 전송하시겠습니까?")
                .setPositiveButton("확인.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(FoodClassify.this,PhotoAdminSendActivity.class);
                        intent.putExtra("UserID",UserName);
                        intent.putExtra("filepath",imgPath);
                        intent.putExtra("photo",bitmap);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP); // 현재 Activity 없애고 이전 화면을 새로운 화면으로 지정
                        startActivity(intent);
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



        // resizes bitmap to given dimensions
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }




    // 음식 정보 찾기 웹통신
    public class FindFood extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            //URL 설정.

            // GET 옵션으로 보낸다.
            String target = "http://graduateproject.dothome.co.kr/FoodFind.php?foodname="+foodname;
            Log.d("FoodFindFoodName", foodname);

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
                Log.d("FoodFindsuccess", success + "");

                // 위젯 참조
                FoodInfo = findViewById(R.id.dbinfo);
                AverageCal = findViewById(R.id.dbcalo);


                if(success) {
                    // 음식정보를 가져온다.
                    foodinfo = jsonResponse.getString("foodinfo");
                    averagecal = jsonResponse.getString("averagecal");

                    FoodInfo.setText(foodinfo);
                    AverageCal.setText(averagecal);
                } else {
                    FoodInfo.setText("아직 미정");
                    AverageCal.setText("아직 미정");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }





}

















