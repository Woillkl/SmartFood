package com.sentilab.testapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<GalleryItem> galleryItems;
    String filePath, UserName;


    public GalleryAdapter(LayoutInflater inflater, ArrayList<GalleryItem> galleryItems) {
        this.inflater = inflater;
        this.galleryItems = galleryItems;
    }




    @Override
    public int getCount() {
        return galleryItems.size();
    }

    @Override
    public Object getItem(int position) {
        return galleryItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        if(view == null) {
            view = inflater.inflate(R.layout.gallery_item,viewGroup,false);
        }

        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvKcal = view.findViewById(R.id.tv_kcal);
        ImageView iv = view.findViewById(R.id.iv);


        final GalleryItem galleryItem = galleryItems.get(position);
        tvName.setText(galleryItem.getFoodName());
        tvDate.setText(galleryItem.getDate());
        tvTime.setText(galleryItem.getTime());
        tvKcal.setText(galleryItem.getKcal() + "kcal");




        // 네트워크에 있는 이미지 읽어오기
        Glide.with(view).load(galleryItem.getImgPath()).into(iv);


        return view;
    }





}
