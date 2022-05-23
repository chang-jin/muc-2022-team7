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

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.snu.muc.dogeeye.common.Logger;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.databinding.ActivityMainBinding;
import com.snu.muc.dogeeye.ui.RecordActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1234;

    private ActivityMainBinding binding;
    private TextSpeechModule mTTSModule;

    private static final Logger log = new Logger();

    private static final int GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 200;
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENT_UI = 9003;

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

        mTTSModule = TextSpeechModule.getInstance();
        mTTSModule.init(this, Locale.US);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startRecording.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);
        });

        binding.gallery.setOnClickListener(view -> {
            // TODO : Change to gallery
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);
        });

        binding.achievement.setOnClickListener(view -> {
            showAchievements();
        });

        binding.leaderboard.setOnClickListener(view -> {
            showLeaderboard();
        });


        PlayGamesSdk.initialize(this);

        checkUserSignIn();
        checkLocationPermission();
    }

    private void checkUserSignIn() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(this);
        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                log.d("is Authenticated");
            } else {
                log.d("is not authenticated ");
            }
        });
        PlayGames.getPlayersClient(this).getCurrentPlayer().
                addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                log.d(task.getResult().getDisplayName() + " Welcome!");
                            }
                        }
                );
    }


    private void showAchievements() {
        PlayGames.getAchievementsClient(this)
                .getAchievementsIntent()
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI));
    }

    private void showLeaderboard() {
        log.d("displayLeaderBoard");
        try {
            PlayGames.getLeaderboardsClient(this)
                    .getLeaderboardIntent(getString(R.string.leaderboard_id))
                    .addOnSuccessListener(intent -> {
                        log.d("onSuccess");
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    })
                    .addOnFailureListener(e -> log.d("onFailure"));
        } catch (Exception e) {
            log.e("Error = " + e.getMessage());
        }
    }

}