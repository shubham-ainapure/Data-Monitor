package com.example.datamonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView btmNavBar;
    String permissions[] = {"android.permission.READ_PHONE_STATE"};
    private static final int REQ_CODE = 1;
    PackageManager packageManager;
    Dialog dialog;

    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btmNavBar = findViewById(R.id.btmNavBar);

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.permission_dialog);
        btn=dialog.findViewById(R.id.btn);

        dialog.setCancelable(false);
        if(!hasUsageStatsPermission()){
            dialog.show();
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, REQ_CODE);
                    }

                    checkUsageStatsPermission();
                }
            });
        }else {
            checkUsageStatsPermission();
        }



        btmNavBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home) {
                    detach();
                    addFragment(new HomeFragment());
                }
                else if (id == R.id.dataUsage)
                    addFragment(new UsageFragment());
                else {
                    detach();
                    addFragment(new SettingFragment());
                }
                return true;
            }
        });

    }

    public void addFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framelyout, fragment);
        ft.commit();
    }
    public void detach(){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(new UsageFragment());
        ft.commit();
    }

    private void checkUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            dialog.dismiss();
            addFragment(new HomeFragment());
        }
    }

    private boolean hasUsageStatsPermission() {
        try {
            packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (hasUsageStatsPermission()) {
                    dialog.dismiss();
                    addFragment(new HomeFragment());
                } else {
                    checkUsageStatsPermission();
                }
            } else {
                Toast.makeText(this, "Both permissions required to function", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
