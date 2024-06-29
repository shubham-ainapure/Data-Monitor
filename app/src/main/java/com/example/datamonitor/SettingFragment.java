package com.example.datamonitor;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.lang.reflect.Method;


public class SettingFragment extends Fragment {
Context context;
Switch mobileDataSwitch,wifiSwitch,hotspotSwitch,notificationSwitch;
WifiManager wifiManager;
ConnectivityManager connectivityManager;

NotificationManager notificationManager;


    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view= inflater.inflate(R.layout.fragment_setting, container, false);

       mobileDataSwitch=view.findViewById(R.id.mobileDataSwitch);
       wifiSwitch=view.findViewById(R.id.wifiSwitch);
       hotspotSwitch=view.findViewById(R.id.hotspotSwitch);
       notificationSwitch=view.findViewById(R.id.notificationSwitch);

       notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
       wifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

       mobileDataSwitch.setChecked(isMobileDataEnabled());
       wifiSwitch.setChecked(wifiManager.isWifiEnabled());
       hotspotSwitch.setChecked(isHotspotEnabled());
       notificationSwitch.setChecked(foregroundServiceRunning());



        Intent intent=new Intent(context, NotificationService.class);


        mobileDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                }else {
                    startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                }
                mobileDataSwitch.setChecked(isMobileDataEnabled());
            }
        });
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                }else {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
                wifiSwitch.setChecked(wifiManager.isWifiEnabled());
            }
        });
        hotspotSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hotspotSwitch.setChecked(isHotspotEnabled());
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(!foregroundServiceRunning()){
                        ContextCompat.startForegroundService(context,intent);
                    }
                }else{
                    Intent stopIntent = new Intent(context, NotificationService.class);
                    stopIntent.setAction("STOP_SERVICE");
                    ContextCompat.startForegroundService(context, stopIntent);
                }
            }
        });

        return view;
    }

    private boolean isHotspotEnabled() {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int state = (int) method.invoke(wifiManager);
            return state == 13; // WifiManager.WIFI_AP_STATE_ENABLED (value 13)
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isMobileDataEnabled() {
        try {
            Method method = connectivityManager.getClass().getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(connectivityManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean foregroundServiceRunning(){
        ActivityManager activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(NotificationService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
           return false;
    }


}