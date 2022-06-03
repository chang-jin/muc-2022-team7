
package com.snu.muc.dogeeye.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.games.PlayGames;
import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.Logger;
import com.snu.muc.dogeeye.common.QuestChecker;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.model.Quest;

import java.util.List;

public class FinishActivity extends AppCompatActivity {
    private static final Logger log = new Logger();

    private Button finish;
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
            startActivity(intent);
        });

        TextView tvStep = findViewById(R.id.totalStep);
        tvStep.setText(String.format("%d steps", (int) current.getTotalStep()));
        TextView tvDistance = findViewById(R.id.totalDistance);
        tvDistance.setText(String.format("%s KM", current.getEveryMovingDistance()));

        // TODO : Photo container setup
        if (true) {
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

        // TODO : Add sharing feature

        // Daily Summary
        speakDailySummary((int) current.getTotalStep(), current.getEveryMovingDistance(), 0, achieved.size());
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