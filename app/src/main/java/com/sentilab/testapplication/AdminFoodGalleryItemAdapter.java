package com.sentilab.testapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdminFoodGalleryItemAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<AdminFoodGalleryItem> adminFoodGalleryItems;

    public AdminFoodGalleryItemAdapter(LayoutInflater inflater, ArrayList<AdminFoodGalleryItem> adminFoodGalleryItems) {
        this.inflater = inflater;
        this.adminFoodGalleryItems = adminFoodGalleryItems;
    }

    @Override
    public int getCount() {
        return adminFoodGalleryItems.size();
    }

    @Override
    public Object getItem(int position) {
        return adminFoodGalleryItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflater.inflate(R.layout.admin_gallery_list_item, viewGroup, false);

        }

        TextView tvFood = view.findViewById(R.id.tv_foodname);

        AdminFoodGalleryItem adminFoodGalleryItem = adminFoodGalleryItems.get(position);
        tvFood.setText(adminFoodGalleryItem.getFoodname());

        return view;
    }
}
