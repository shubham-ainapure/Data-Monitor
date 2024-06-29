package com.example.datamonitor;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomeFragment extends Fragment {

NetworkStatsManager networkStatsManager,networkStatsManager1;
Context context;
TextView mobileToday,mobileThisWeek,mobileThisMonth;
TextView wifiToday,wifiThisWeek,wifiThisMonth;
TextView hotspotToday,hotspotWeek,hotspotMonth;
float todaysUsage,weeksUsage,monthsUsage;

String size;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        this.networkStatsManager1=(NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        mobileToday=view.findViewById(R.id.mobileToday);
        mobileThisWeek=view.findViewById(R.id.mobileThisWeek);
        mobileThisMonth=view.findViewById(R.id.mobileThisMonth);

        wifiToday=view.findViewById(R.id.wifiToday);
        wifiThisWeek=view.findViewById(R.id.wifiThisWeek);
        wifiThisMonth=view.findViewById(R.id.wifiThisMonth);

        hotspotToday=view.findViewById(R.id.hotspotToday);
        hotspotWeek=view.findViewById(R.id.hotspotWeek);
        hotspotMonth=view.findViewById(R.id.hotspotMonth);

        long todays = getTodayDataUsage(ConnectivityManager.TYPE_MOBILE);
        long weeks = getThisWeekDataUsage(ConnectivityManager.TYPE_MOBILE);
        long  months = getThisMonthDataUsage(ConnectivityManager.TYPE_MOBILE);
        todaysUsage=(float)todays;
        weeksUsage=(float)weeks;
        monthsUsage=(float)months;

        mobileToday.setText(dataConversion(todaysUsage)+" "+size);
        mobileThisWeek.setText(dataConversion(weeksUsage)+" "+size);
        mobileThisMonth.setText(dataConversion(monthsUsage)+" "+size);

         todays = getTodayDataUsage(ConnectivityManager.TYPE_WIFI);
         weeks = getThisWeekDataUsage(ConnectivityManager.TYPE_WIFI);
         months = getThisMonthDataUsage(ConnectivityManager.TYPE_WIFI);
        todaysUsage=(float)todays;
        weeksUsage=(float)weeks;
        monthsUsage=(float)months;

        wifiToday.setText(dataConversion(todaysUsage)+" "+size);
        wifiThisWeek.setText(dataConversion(weeksUsage)+" "+size);
        wifiThisMonth.setText(dataConversion(monthsUsage)+" "+size);

        todays = getTodayHotspotDataUsage();
        weeks = getThisWeekHotspotDataUsage();
        months = getThisMonthHotspotDataUsage();
        todaysUsage=(float)todays;
        weeksUsage=(float)weeks;
        monthsUsage=(float)months;

        hotspotToday.setText(dataConversion(todaysUsage)+" "+size);
        hotspotWeek.setText(dataConversion(weeksUsage)+" "+size);
        hotspotMonth.setText(dataConversion(monthsUsage)+" "+size);

        return view;
    }

    public String getSubscriberId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return telephonyManager.getSubscriberId();
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public long getDataUsage(int networkType, long startTime, long endTime) {
        String subId=getSubscriberId();
        NetworkStats networkStats;
        long totalBytes = 0;

        try {
            networkStats = networkStatsManager.querySummary(networkType,subId , startTime, endTime);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                totalBytes += bucket.getRxBytes() + bucket.getTxBytes();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return totalBytes;
    }
    public long getTodayDataUsage(int networkType) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getDataUsage(networkType, startTime, endTime);
    }

    public long getThisWeekDataUsage(int networkType) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getDataUsage(networkType, startTime, endTime);
    }

    public long getThisMonthDataUsage(int networkType) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getDataUsage(networkType, startTime, endTime);
    }
    public long getHotspotDataUsage(long startTime, long endTime) {
        NetworkStats networkStats;
        String subId=getSubscriberId();
        long totalBytes = 0;
        long rxBytes = 0;
        long txBytes = 0;

        try {
           // Querying all interfaces for tethering usage
           networkStats = networkStatsManager1.querySummary(ConnectivityManager.TYPE_MOBILE, subId, startTime, endTime);
           NetworkStats.Bucket bucket = new NetworkStats.Bucket();



           while (networkStats.hasNextBucket()) {
//            bucket = new NetworkStats.Bucket();
               networkStats.getNextBucket(bucket);
               if(bucket.getUid()==NetworkStats.Bucket.UID_TETHERING) {
                   rxBytes += bucket.getRxBytes();
                   txBytes += bucket.getTxBytes();
               }
           }
           networkStats.close();
       }catch (Exception e){
           e.printStackTrace();
       }


        return txBytes;
    }

    public long getTodayHotspotDataUsage() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getHotspotDataUsage(startTime, endTime);
    }

    public long getThisWeekHotspotDataUsage() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getHotspotDataUsage(startTime, endTime);
    }

    public long getThisMonthHotspotDataUsage() {
        Calendar calendar = Calendar.getInstance();
        long endTime=calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        return getHotspotDataUsage(startTime, endTime);
    }
    public String dataConversion(float data){
        int i;
        for( i=0;i<5;i++){
            if(data>1000){
                data=data/1024;
            }
            else {
                break;
            }
        }
//        i--;
        if(i==0)
            size="KB";
        else if (i==1)
            size="KB";
        else if (i==2)
            size="MB";
        else if(i==3)
            size="GB";
        else
            size="TB";

        String formattedResult = String.format("%.1f", data);
        return formattedResult;
    }
}