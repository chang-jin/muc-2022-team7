package com.snu.muc.dogeeye.ui.logs;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class detailLogs extends AppCompatActivity {

    private ProjectDao pDao;
    private ProjectDB pdb;
    private TextSpeechModule module = null;

    private ImageView[] imageViewList;

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

        imageViewList = new ImageView[3];
        imageViewList[0] = findViewById(R.id.contentImage1);
        imageViewList[1] = findViewById(R.id.contentImage2);
        imageViewList[2] = findViewById(R.id.contentImage3);

        TextView activityId = findViewById(R.id.detailId);
        activityId.setText("Activity #"+orderID);

        TextView totalLogCount = findViewById(R.id.totalLogs);
        totalLogCount.setText("Total : " + detailedLog.size() + " Logs");

        TextView detailedLogSummary = findViewById(R.id.detailLogs);
        detailedLogSummary.setText(String.format("%.2f", project.getStart2MaxDistance()) + "m" + " / "  + project.getTotalStep() + " Steps" + " / " + detailedPhotos.size() + " Photos");

        for(int i = 0 ; i < detailedPhotos.size() && i < 3 ; ++i)
        {
            File imageFile = new File(getExternalMediaDirs()[0],
                    detailedPhotos.get(i).getFileName());

            imageViewList[i].setImageURI(
                    Uri.fromFile(imageFile));
        }

        RecyclerView recyclerView = findViewById(R.id.logContents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL, false));

        DetailAdaptor adaptor = new DetailAdaptor(detailedLog);
        recyclerView.setAdapter(adaptor);


        StringBuilder sb = new StringBuilder();

        String comments = "Opening the " + orderID + "th activity details"+
                " there are "+detailedLog.size() + " logs to check";

        module.textToSpeech(comments);
    }
}