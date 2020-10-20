package com.sentilab.testapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<GalleryItem> galleryItems;

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
    public View getView(int position, View view, ViewGroup viewGroup) {

        if(view == null) {
            view = inflater.inflate(R.layout.gallery_item,viewGroup,false);
        }

        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvKcal = view.findViewById(R.id.tv_kcal);
        ImageView iv = view.findViewById(R.id.iv);

        GalleryItem galleryItem = galleryItems.get(position);
        tvName.setText(galleryItem.getFoodName());
        tvDate.setText(galleryItem.getDate());
        tvTime.setText(galleryItem.getTime());
        tvKcal.setText(galleryItem.getKcal() + "kcal");

        // 네트워크에 있는 이미지 읽어오기
        Glide.with(view).load(galleryItem.getImgPath()).into(iv);

        return view;
    }
}
