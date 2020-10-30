package com.sentilab.testapplication;

import android.graphics.drawable.Drawable;

public class GalleryItem {

    int bno;
    String UserName;
    String FoodName;
    String imgPath;
    String date;
    String time;
    String kcal;
    String filePath;

    public GalleryItem(int bno, String FoodName, String imgPath, String time, String kcal, String date, String UserName, String filePath) {
        this.bno = bno;
        this.FoodName = FoodName;
        this.imgPath = imgPath;
        this.time = time;
        this.kcal = kcal;
        this.date = date;
        this.UserName = UserName;
        this.filePath = filePath;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKcal() {
        return kcal;
    }

    public void setKcal(String kcal) {
        this.kcal = kcal;
    }


    public int getBno() {
        return bno;
    }

    public String getUserName() {
        return UserName;
    }

    public String getFoodName() {
        return FoodName;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getDate() {
        return date;
    }

    public void setBno(int bno) {
        this.bno = bno;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setFoodName(String foodName) {
        FoodName = foodName;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
