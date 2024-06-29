package com.example.datamonitor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "DataUsageChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long DATA_USAGE_THRESHOLD = 10 * 1024 * 1024; // 10MB
    private boolean isServiceRunning=true;
    private NetworkStatsManager networkStatsManager;
    private ExecutorService executorService;
    NotificationManager notificationManager;
    private long lastDataUsage = 0;
    String size;
    float data;
    @Override
    public void onCreate() {
        super.onCreate();
        networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
        executorService = Executors.newSingleThreadExecutor();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification("Initializing..."));
        monitorDataUsage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && intent.getAction()!=null && intent.getAction().equals("STOP_SERVICE")){
            stopSelf();
            return START_NOT_STICKY;
        }else{
            return START_STICKY;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Data Usage Channel";
            String description = "Channel for Data Usage Service";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
             notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Data Usage Monitor")
                .setContentText(content)
                .setSmallIcon(R.drawable.baseline_data_usage_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private android.app.Notification getNotification(String content) {
        return getNotificationBuilder(content).build();
    }

    private void monitorDataUsage() {
        executorService.execute(() -> {
            while (isServiceRunning) {
                long currentDataUsage = getCurrentDataUsage();
                 data=(float) currentDataUsage;
                 String usage=dataConversion(data);
//                if (Math.abs(currentDataUsage - lastDataUsage) >= DATA_USAGE_THRESHOLD) {
//                    lastDataUsage = currentDataUsage;
                    updateNotification("Data Usage: " + usage+" "+size);
//                }
                try {
                    Thread.sleep(60000); // Check data usage every 60 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
}

    private long getCurrentDataUsage() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        long dataUsage = 0;

        try {
            String subId = getSubscriberId();
            NetworkStats networkStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, subId, startTime, endTime);
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();

            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
//                if (bucket.getUid() == android.os.Process.myUid()) {
                    dataUsage += (bucket.getRxBytes() + bucket.getTxBytes());
//                }
            }
            networkStats.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataUsage;
    }

    private String getSubscriberId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                return telephonyManager.getSubscriberId();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void updateNotification(String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, getNotification(content));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Intent restart=new Intent(this, NotificationService.class);
//        PendingIntent restartPending=PendingIntent.getService(this,1,restart, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
//        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set( AlarmManager.ELAPSED_REALTIME,
//                SystemClock.elapsedRealtime() + 1000,
//                restartPending);
//        executorService.shutdownNow();
        isServiceRunning = false;
        executorService.shutdownNow();
    }


    public String dataConversion(float data) {
        int i;
        for (i = 0; i < 5; i++) {
            if (data > 1000) {
                data = data / 1024;
            } else {
                break;
            }
        }
//        i--;
        if (i == 0)
            size = "KB";
        else if (i == 1)
            size = "KB";
        else if (i == 2)
            size = "MB";
        else if (i == 3)
            size = "GB";
        else
            size = "TB";

        String formattedResult = String.format("%.1f", data);
        return formattedResult;
    }
}
