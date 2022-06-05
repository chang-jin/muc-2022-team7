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
import com.snu.muc.dogeeye.model.LogEntity;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import java.util.ArrayList;
import java.util.List;

public class detailLogs extends AppCompatActivity {

    private ProjectDao pDao;
    private ProjectDB pdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_logs);

        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();

        Intent curIntent = getIntent();
        int pid = curIntent.getIntExtra("curProj",0);

        Toast.makeText(this,"proj ID : " + pid,Toast.LENGTH_SHORT).show();


        List<LogEntity> detailedLog = pDao.getLogByPID(pid);

        for(int i = 0 ; i < detailedLog.size() ; ++i)
            Log.d("detailedLog","val : " + detailedLog.get(i).getLID());

        TextView textView = findViewById(R.id.entityStart);
        String startTime = detailedLog.get(0).getLogTime().split(" ")[1];
        startTime = startTime.split(":")[0] + " : " + startTime.split(":")[1];
        textView.setText(startTime);

        RecyclerView recyclerView = findViewById(R.id.logContents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL, false));

        DetailAdaptor adaptor = new DetailAdaptor(detailedLog);
        recyclerView.setAdapter(adaptor);


        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(detailedLog.size());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(50);

                Log.d("bar", "onProgressChanged: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}