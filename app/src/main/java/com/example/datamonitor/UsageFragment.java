package com.example.datamonitor;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UsageFragment extends Fragment {
    Toolbar toolbar;
    ArrayList<AppModel> appModel=new ArrayList<>();
    ArrayList<String> spinItem=new ArrayList<>();
    PackageManager packageManager;
    NetworkStatsManager networkStatsManager;
    ProgressBar pgBar;
    TextView pgText,usageCardText,cardUsage;
    ImageView calenderLogo;
    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Context context;
    HomeFragment homeFragment;
    EditText startDate,endDate;
    Button btn;
    MenuItem mobileUsage,wifiUsage;
    RecyclerAdapter adapter;
    RecyclerView RCV;
    CardView usageCard;
    int startY,startM,startD,endY,endM,endD;

    String date1="",date2="",size;
    int success1,success2;
    int itemId,spinnerId;
    long totalUsage;
    float totalUsage1;

    public UsageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context=context;
        this.networkStatsManager=(NetworkStatsManager)context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_usage, container, false);
        homeFragment=new HomeFragment();
        calenderLogo=view.findViewById(R.id.calenderLogo);
        usageCardText=view.findViewById(R.id.usageCardText);
        cardUsage=view.findViewById(R.id.cardUsage);
        usageCard=view.findViewById(R.id.usageCard);

       spinItem.add("Mobile usage");
       spinItem.add("Wi-Fi usage");


        final Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.date_dialog);
        startDate=dialog.findViewById(R.id.startDate);
        endDate=dialog.findViewById(R.id.endDate);
        btn=dialog.findViewById(R.id.btn);

        Spinner spinner=dialog.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,spinItem);
        spinner.setAdapter(arrayAdapter);
        usageCard.setVisibility(View.GONE);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker1(startDate);
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {datePicker2(endDate);
            }
        });

        calenderLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        toolbar=view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.network_menu);
        Menu menu=toolbar.getMenu();
        MenuItem mobileUsage=menu.findItem(R.id.mobileUsage);
        MenuItem wifiUsage=menu.findItem(R.id.wifiUsage);



        pgBar=view.findViewById(R.id.pgBar);
        pgText=view.findViewById(R.id.pgText);
        pgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.bgcolor), android.graphics.PorterDuff.Mode.SRC_IN);
        pgBar.setVisibility(View.GONE);
        pgText.setVisibility(View.GONE);



        RCV=view.findViewById(R.id.RCV);
        RCV.setLayoutManager(new LinearLayoutManager(context));


        getUsage(ConnectivityManager.TYPE_MOBILE);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                itemId=item.getItemId();
                usageCard.setVisibility(View.GONE);

                if (itemId==R.id.mobileUsage){
                    usageCardText.setText("Mobile Usage Today");
                   getUsage(ConnectivityManager.TYPE_MOBILE);
                }
                else{
                    usageCardText.setText("Wi-Fi Usage Today");
                   getUsage(ConnectivityManager.TYPE_WIFI);
                }
                return true;
            }
        });

//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                spinnerId=position;
//            }
//        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            if(validateDates()){
                usageCard.setVisibility(View.GONE);
                usageCardText.setText(date1+" to "+date2);
                if(spinner.getSelectedItemPosition()==0) {
                    getUsage2(ConnectivityManager.TYPE_MOBILE);
                }
                else {
                    getUsage2(ConnectivityManager.TYPE_WIFI);
                }
                }
            }

        });
        return view;
    }
    public void getUsage(int networkType) {

            appModel.clear();
            RCV.setAdapter(adapter);
            pgBar.setVisibility(View.VISIBLE);
            pgText.setVisibility(View.VISIBLE);
            executorService.execute(() -> {
                appModel = getAllAppUsage(networkType);
                totalUsage = getTotalDataUsage(networkType);
                totalUsage1 = (float) totalUsage;
                if (isAdded() && getActivity()!=null) {
                    requireActivity().runOnUiThread(() -> {
                        adapter = new RecyclerAdapter(context, appModel);
                        RCV.setAdapter(adapter);
                        pgBar.setVisibility(View.GONE);
                        pgText.setVisibility(View.GONE);
                        cardUsage.setText(dataConversion(totalUsage1) + " " + size);
                        usageCard.setVisibility(View.VISIBLE);
                    });
                }
            });
    }
    public void getUsage2(int networkType) {

        appModel.clear();
        RCV.setAdapter(adapter);
        pgBar.setVisibility(View.VISIBLE);
        pgText.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            appModel = getAllAppUsage1(networkType);
            totalUsage = getTotalDataUsage2(networkType, startY, startM, startD, endY, endM, endD);
            totalUsage1 = (float) totalUsage;
            if (isAdded() && getActivity()!=null) {
                requireActivity().runOnUiThread(() -> {
                    adapter = new RecyclerAdapter(context, appModel);
                    RCV.setAdapter(adapter);
                    pgBar.setVisibility(View.GONE);
                    pgText.setVisibility(View.GONE);
                    cardUsage.setText(dataConversion(totalUsage1) + " " + size);
                    usageCard.setVisibility(View.VISIBLE);
                });
            }
        });
    }
    public long getTotalDataUsage(int networkType) {
        Calendar calendar=Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
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
    public long getTotalDataUsage2(int networkType, int startY,int startM,int startD,int endY,int endM,int endD) {
        Calendar calendar=Calendar.getInstance();

        calendar.set(startY,startM,startD,0,0,0);
        long startTime=calendar.getTimeInMillis();
        calendar.set(endY,endM,endD,24,0,0);
        long endTime=calendar.getTimeInMillis();
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
    public ArrayList<AppModel> getAllAppUsage(int networkType){
        PackageManager packageManager=context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);

        for(int i=0;i<packageInfos.size();i++){
            PackageInfo packageInfo=packageInfos.get(i);
            // Get the application icon
            Drawable icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);

            // Get the application label (name)
            String name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();

            // Get the package name
            String packageName = packageInfo.packageName;

            // Get uid
            int uid=packageInfo.applicationInfo.uid;
            long dataUsage;
            dataUsage=getDataUsage(uid,networkType);
            Log.d("usage", String.valueOf(dataUsage));

            //Decide apps category
            String category;
            if((packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM)!=0){
                category="System app";
            }
            else {
                category="Installed app";
            }
            appModel.add(new AppModel(name,category,icon,packageName,dataUsage));
        }
        return appModel;
    }
    public ArrayList<AppModel> getAllAppUsage1(int networkType){
        PackageManager packageManager=context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);

        for(int i=0;i<packageInfos.size();i++){
            PackageInfo packageInfo=packageInfos.get(i);
            // Get the application icon
            Drawable icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);

            // Get the application label (name)
            String name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();

            // Get the package name
            String packageName = packageInfo.packageName;

            // Get uid
            int uid=packageInfo.applicationInfo.uid;
            long dataUsage;
            dataUsage=getDataUsage(uid,networkType,startY,startM,startD,endY,endM,endD);
            Log.d("usage", String.valueOf(dataUsage));

            //Decide apps category
            String category;
            if((packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM)!=0){
                category="System app";
            }
            else {
                category="Installed app";
            }
            appModel.add(new AppModel(name,category,icon,packageName,dataUsage));
        }
        return appModel;
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

    private long getDataUsage(int uid,int networkType) {

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        long dataUsage=0;

        NetworkStats networkStats;

        try {
            String subId=getSubscriberId();
            networkStats=networkStatsManager.querySummary(networkType,subId,startTime,System.currentTimeMillis());
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                if (bucket.getUid() == uid) {
                    dataUsage = (bucket.getRxBytes() +bucket.getTxBytes());
                }
            }

            networkStats.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataUsage;
    }
    private long getDataUsage(int uid,int networkType,int startY,int startM,int startD,int endY,int endM,int endD) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(startY,startM,startD,0,0,0);
        long startTime = calendar.getTimeInMillis();
        calendar.set(endY,endM,endD,24,0,0);
        long endTime=calendar.getTimeInMillis();

        long dataUsage=0;
        NetworkStats networkStats;

        try {
            String subId=getSubscriberId();
            networkStats=networkStatsManager.querySummary(networkType,subId,startTime,endTime);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                if (bucket.getUid() == uid) {
                    dataUsage = (bucket.getRxBytes() +bucket.getTxBytes());
                }
            }

            networkStats.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataUsage;
    }

    public int datePicker1(EditText editText){
        DatePickerDialog datePickerDialog=new DatePickerDialog(context);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-94);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editText.setText(String.valueOf(dayOfMonth)+"-"+String.valueOf(month+1)+"-"+String.valueOf(year));
                date1=dayOfMonth+"-"+(month+1)+"-"+year;
                startY=year;
                startM=month;
                startD=dayOfMonth;
            }
        });
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
        if(startY>0)
            return 1;
        else
            return 0;
    }
    public int datePicker2(EditText editText){
        DatePickerDialog datePickerDialog=new DatePickerDialog(context);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-94);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                view.setMaxDate(Calendar.DAY_OF_YEAR);
                editText.setText(String.valueOf(dayOfMonth)+"-"+String.valueOf(month+1)+"-"+String.valueOf(year));
                date2=dayOfMonth+"-"+(month+1)+"-"+year;
                endY=year;
                endM=month;
                endD=dayOfMonth;
            }
        });
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
        if(endY>0)
            return 1;
        else
            return 0;
    }
    private boolean validateDates() {
        if (startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()) {
            Toast.makeText(context,"please choose a date",Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "date cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (endM<startM || endD<startD && startM==endM) {
            Toast.makeText(context,"Wrong date order",Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
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

//
}