
package com.snu.muc.dogeeye.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.games.PlayGames;
import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.Logger;
import com.snu.muc.dogeeye.common.QuestChecker;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.common.PhotoStamp;
import com.snu.muc.dogeeye.model.PhotoEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.model.Quest;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class FinishActivity extends AppCompatActivity {
    private static final Logger log = new Logger();

    private Button finish;
    private Button share;
    private QuestChecker mQuestChecker;
    private ProjectDB projectDb;
    private ProjectDao projectDao;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        Intent myIntent = getIntent();
        int currentProjectId = myIntent.getIntExtra("currentProjectId", 0);

        projectDb = ProjectDB.getProjectDB(this);
        projectDao = projectDb.projectDao();
        Project current = projectDao.getProjectsByID(currentProjectId);

        finish = findViewById(R.id.finish);
        finish.setOnClickListener(view -> {
            Intent intent = new Intent(FinishActivity.this, MainActivity.class);
            TextSpeechModule.getInstance().stopTTS();
            startActivity(intent);
        });

        TextView tvStep = findViewById(R.id.totalStep);
        tvStep.setText(String.format("%d steps", (int) current.getTotalStep()));
        TextView tvDistance = findViewById(R.id.totalDistance);
        tvDistance.setText(String.format("%.2f M", current.getEveryMovingDistance()));

        // Photo Container Setup
        List<PhotoEntity> photos = projectDao.getPhotoEntities(currentProjectId);
        if (photos.isEmpty()) {
            LinearLayout photoContainer = findViewById(R.id.photoContainer);
            photoContainer.setVisibility(View.GONE);
        }

        // Achievement Container Setup
        mQuestChecker = new QuestChecker(this, currentProjectId);
        List<Quest> achieved = mQuestChecker.getNewlyAchievedQuests();
        achieved.forEach(quest -> {
            PlayGames.getAchievementsClient(this).unlock(quest.getId());
        });

        RecyclerView recyclerView = findViewById(R.id.achievementList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AchievementAdapter(this, achieved));

        if (achieved.isEmpty()) {
            LinearLayout achievementContainer = findViewById(R.id.achievementContainer);
            achievementContainer.setVisibility(View.GONE);
        }

        share = findViewById(R.id.share);
        share.setOnClickListener(view -> {
            shareTheWalk(current, photos);
        });

        // Daily Summary
        speakDailySummary((int) current.getTotalStep(), current.getEveryMovingDistance(), 0, achieved.size());
    }

    private Bitmap getImageToShare(List<PhotoEntity> photos) {
        if (photos.isEmpty()) {
            Bitmap target = BitmapFactory.decodeResource(getResources(), R.drawable.walk_background).copy(Bitmap.Config.ARGB_8888, true);
            return Bitmap.createScaledBitmap(target, 1000, 1000, false);
        } else {
            String imagePath = photos.get(0).getFilePath();
            String fileName = photos.get(0).getFileName();

            File imgFile = new File(imagePath + File.separator + fileName);
            if (imgFile.exists()) {
                Bitmap target = BitmapFactory.decodeFile(imgFile.getAbsolutePath()).copy(Bitmap.Config.ARGB_8888, true);
                return Bitmap.createScaledBitmap(target, 1000, 1000, false);
            } else {
                Bitmap target = BitmapFactory.decodeResource(getResources(), R.drawable.walk_background).copy(Bitmap.Config.ARGB_8888, true);
                return Bitmap.createScaledBitmap(target, 1000, 1000, false);
            }
        }
    }

    private void shareTheWalk(Project current, List<PhotoEntity> photos) {
        Bitmap imageToShare = getImageToShare(photos);

        PhotoStamp stamper = new PhotoStamp();
        stamper.stamp_random(imageToShare, current);

        // Share the image through
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageToShare.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(getFilesDir() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri imageUri = FileProvider.getUriForFile(
                this,
                "com.snu.muc.dogeeye.provider",
                f);
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        Intent chooser = Intent.createChooser(share, "Share Image");
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getApplicationContext().grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(chooser);
    }

    @SuppressLint("DefaultLocale")
    private void speakDailySummary(int steps, float distance, int photoSize, int questSize) {
        TextSpeechModule module = TextSpeechModule.getInstance();

        StringBuilder sb = new StringBuilder();
        // 인사
        sb.append("Congratulations!");

        // 정보 1. 오늘 총 걸음 수
        sb.append(String.format("Today, you have walked %d steps in total.", steps));

        // 정보 2. 오늘 총 걸은 거리
        sb.append(String.format("And, you have walked a total distance of %s kilometers today.", distance));

        // 정보 3. 오늘 찍은 사진
        if (photoSize != 0) {
            sb.append(String.format("And, you took a total of %d pictures today.", photoSize));
        }

        // 정보 4. 오늘 획득한 업적
        if (questSize != 0) {
            sb.append(String.format("And, you have achieved a total of %d achievements today.", questSize));
        }

        module.textToSpeech(sb.toString());
    }
}