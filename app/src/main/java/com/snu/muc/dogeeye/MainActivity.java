package com.snu.muc.dogeeye;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.snu.muc.dogeeye.common.Logger;
import com.snu.muc.dogeeye.common.QuestChecker;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.databinding.ActivityMainBinding;
import com.snu.muc.dogeeye.model.Quest;
import com.snu.muc.dogeeye.ui.GalleryActivity;
import com.snu.muc.dogeeye.ui.RecordActivity;
import com.snu.muc.dogeeye.ui.SuggestActivity;
import com.snu.muc.dogeeye.ui.TestActivity;
import com.snu.muc.dogeeye.ui.logs.logsActivity;

import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TextSpeechModule mTTSModule;

    private static final Logger log = new Logger();

    private static final int GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENT_UI = 9003;

    String[] random_encouragements = new String[]{
            "You look better than ever!",
            "Another day of sun has risen.",
            "Never gonna give you up!",
            "An adventure awaits you!"
    };

    private void checkPermission() {
        int accessLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int accessActivity = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION);
        int accessCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (accessLocation == PackageManager.PERMISSION_DENIED ||
            accessActivity == PackageManager.PERMISSION_DENIED ||
            accessCamera == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, GPS_UTIL_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //check if it's the first launch
    private boolean checkIfFirstRun() {
        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;
        // Get the current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        // Get the saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
        // Check for first run or upgrade

        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            Toast.makeText(this,"This is a normal run",Toast.LENGTH_LONG).show();
            Log.d("checkIfFirstRun", "This is a normal run");

            return false;

        } else if (savedVersionCode == DOESNT_EXIST) {
            Toast.makeText(this,"This is the first run!",Toast.LENGTH_LONG).show();
            Log.d("checkIfFirstRun", "this is the first run");

            log.d("Achieved = achievement_the_first_launch");
            PlayGames.getAchievementsClient(this).unlock("TODO:FIX THE ID");
            log.d("Achieved = achievement_the_first_launch");


            return true;
            // TODO This is a new install (or the user cleared the shared preferences)
        } else if (currentVersionCode > savedVersionCode) {
            Toast.makeText(this,"This is an updated run",Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
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
        checkIfFirstRun();
        super.onCreate(savedInstanceState);

        QuestChecker questChecker = new QuestChecker(this, -1);
        double[] totalDistanceAndSteps = questChecker.getTotalDistanceAndSteps();
        String encouragement = random_encouragements[new Random().nextInt(random_encouragements.length)];

        String summary = encouragement + " You have traveled " + Math.round(totalDistanceAndSteps[0]/1000) + " kms," + Math.round(totalDistanceAndSteps[1]) + " steps in total.";


        mTTSModule = TextSpeechModule.getInstance();
        mTTSModule.init(this, Locale.US);

        checkPermission();


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mainSummaryContent.setText(summary);


        binding.startRecording.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);
        });

        binding.gallery.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        binding.mainSuggest.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SuggestActivity.class);
            startActivity(intent);
        });

        binding.achievement.setOnClickListener(view -> {
            showAchievements();
        });

        binding.leaderboard.setOnClickListener(view -> {
            showLeaderboard();
        });

        binding.mainSummary.setOnClickListener(view->{
            Intent intent = new Intent(MainActivity.this, logsActivity.class);
            startActivity(intent);
        });

//            binding.test.setOnClickListener(view -> {
//                // TODO : Change to gallery
//                Intent intent = new Intent(MainActivity.this, TestActivity.class);
//                startActivity(intent);
//            });


        PlayGamesSdk.initialize(this);

        checkUserSignIn();
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