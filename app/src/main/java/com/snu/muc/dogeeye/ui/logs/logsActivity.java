package com.snu.muc.dogeeye.ui.logs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.databinding.ActivityLogsBinding;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import java.util.ArrayList;
import java.util.Collections;

public class logsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EntityAdaptor adapter;
    private ProjectDao pDao;
    private ProjectDB pdb;
    ArrayList<Project> projectList;
    ActivityLogsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        Button button = findViewById(R.id.back_main);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(logsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();

        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL, false));

        projectList = (ArrayList<Project>) pDao.getAllProjects();
        Collections.reverse(projectList);
        ArrayList<logEntity> dateAddedProject = logEntity.getEntityList(projectList);

        for(int i = 0 ; i < dateAddedProject.size() ; ++i)
        {
            Log.d("revised", dateAddedProject.get(i).getType() +"__"+dateAddedProject.get(i).getDate());
        }

        adapter = new EntityAdaptor(dateAddedProject,getApplicationContext());

        recyclerView.setAdapter(adapter);

    }
}