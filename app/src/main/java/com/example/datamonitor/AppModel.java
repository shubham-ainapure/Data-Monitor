package com.example.datamonitor;

import android.graphics.drawable.Drawable;

public class AppModel {
    String appName;
    Drawable appIcon;
    String category;
    String packageName;

   long dataUsage;

    public AppModel(String appName, String category, Drawable appIcon, String packageName, long dataUsage) {
        this.appName = appName;
        this.category=category;
        this.appIcon = appIcon;
        this.packageName = packageName;
        this.dataUsage=dataUsage;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }


    public String getPackageName() {
        return packageName;
    }
    public String getCategory(){
        return category;
    }

    public long getDataUsage(){return dataUsage;}

}