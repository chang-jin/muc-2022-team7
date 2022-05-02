package com.snu.muc.dogeeye;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.snu.muc.dogeeye.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1234;
    private ActivityMainBinding binding;

    public static ttsModule main_tts;
    public static sttModule main_stt;

    private static final int GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 200;

    private void checkLocationPermission() {
        int accessLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int accessActivity = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION);

        if (accessLocation == PackageManager.PERMISSION_DENIED || accessActivity == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION,Manifest.permission.INTERNET}, GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("위치 권한이 꺼져있습니다.");
                        builder.setMessage("[권한] 설정에서 위치 권한을 허용해야 합니다.");
                        builder.setPositiveButton("설정으로 가기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }).setNegativeButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_photo, R.id.navigation_record, R.id.navigation_quest)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        PlayGamesSdk.initialize(this);

        checkLocationPermission();


        main_tts = new ttsModule(this, Locale.US);
        main_stt = new sttModule(this, Locale.US);
    }
}