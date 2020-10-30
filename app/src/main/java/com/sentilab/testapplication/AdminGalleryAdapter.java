package com.sentilab.testapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdminGalleryAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<AdminGalleryItem> adminGalleryItems;

    public AdminGalleryAdapter(LayoutInflater inflater, ArrayList<AdminGalleryItem> adminGalleryItems) {
        this.inflater = inflater;
        this.adminGalleryItems = adminGalleryItems;
    }

    @Override
    public int getCount() {
        return adminGalleryItems.size();
    }

    @Override
    public Object getItem(int position) {
        return adminGalleryItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.admin_gallery_item, viewGroup, false);
        }

        ImageView iv = view.findViewById(R.id.iv);

        AdminGalleryItem adminGalleryItem = adminGalleryItems.get(position);

        Glide.with(view).load(adminGalleryItem.getImgPath()).into(iv);

        return view;
    }


}
