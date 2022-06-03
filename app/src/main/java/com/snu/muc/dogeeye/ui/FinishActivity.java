package com.snu.muc.dogeeye.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.QuestChecker;

public class FinishActivity extends AppCompatActivity {

    Button finish;
    QuestChecker mQuestChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        Intent myIntent = getIntent();
        int currentProjectId = myIntent.getIntExtra("currentProjectId", 0);

        finish = findViewById(R.id.finish);
        finish.setOnClickListener(view -> {
            Intent intent = new Intent(FinishActivity.this, MainActivity.class);
            startActivity(intent);
        });

        mQuestChecker = new QuestChecker(this, currentProjectId);
        mQuestChecker.getNewlyAchievedQuests();
    }
}