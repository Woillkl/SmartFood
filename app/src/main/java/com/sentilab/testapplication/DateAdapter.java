package com.sentilab.testapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStreamReader;
import java.util.ArrayList;

public class DateAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<DateItem> dateItems;

    public DateAdapter(LayoutInflater inflater, ArrayList<DateItem> dateItems) {
        this.inflater = inflater;
        this.dateItems = dateItems;
    }

    @Override
    public int getCount() {
        return dateItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dateItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflater.inflate(R.layout.date_item, viewGroup, false);
        }

        TextView tvDate = view.findViewById(R.id.tv_date);

        DateItem dateItem = dateItems.get(position);
        tvDate.setText(dateItem.getDate());

//        LinearLayout DateClick = view.findViewById(R.id.DateClick);
//        DateClick.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ListDateActivity.this,GalleryActivity.class);
//            }
//        });

        return view;
    }
}
