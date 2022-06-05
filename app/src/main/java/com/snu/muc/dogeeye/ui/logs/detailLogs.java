package com.snu.muc.dogeeye.ui.logs;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.common.TextSpeechModule;
import com.snu.muc.dogeeye.model.LogEntity;
import com.snu.muc.dogeeye.model.PhotoEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class detailLogs extends AppCompatActivity {

    private ProjectDao pDao;
    private ProjectDB pdb;
    private TextSpeechModule module = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_logs);

        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();

        Intent curIntent = getIntent();
        int pid = curIntent.getIntExtra("curProj",0);
        int orderID = curIntent.getIntExtra("orderedNumber",0);

        Project project = pDao.getProjectsByID(pid);

        List<LogEntity> detailedLog = pDao.getLogByPID(pid);
        List<PhotoEntity> detailedPhotos = pDao.getPhotoEntities(pid);

        module = TextSpeechModule.getInstance();

        TextView textView = findViewById(R.id.entityStart);
        String startTime = detailedLog.get(0).getLogTime().split(" ")[1];
        startTime = startTime.split(":")[0] + " : " + startTime.split(":")[1];
        textView.setText(startTime);

        TextView activityId = findViewById(R.id.detailId);
        activityId.setText("Activity #"+orderID);

        TextView totalLogCount = findViewById(R.id.totalLogs);
        totalLogCount.setText("Total : " + detailedLog.size() + " Logs");

        TextView detailedLogSummary = findViewById(R.id.detailLogs);
        detailedLogSummary.setText(String.format("%.2f", project.getStart2MaxDistance()) + "m" + " / "  + project.getTotalStep() + " Steps" + " / " + detailedPhotos.size() + " Photos");

        RecyclerView recyclerView = findViewById(R.id.logContents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL, false));

        DetailAdaptor adaptor = new DetailAdaptor(detailedLog);
        recyclerView.setAdapter(adaptor);


        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(detailedLog.size()-1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(50);

                recyclerView.scrollToPosition(i);

                Log.d("bar", "onProgressChanged: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        StringBuilder sb = new StringBuilder();

        String comments = "Opening the " + orderID + "th activity details"+
                " there are "+detailedLog.size() + " logs to check";

        module.textToSpeech(comments);
    }
}