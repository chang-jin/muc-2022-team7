package com.snu.muc.dogeeye.ui.logs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.snu.muc.dogeeye.R;

public class detailLogs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_logs);

        Intent curIntent = getIntent();
        int pid = curIntent.getIntExtra("curProj",0);

        Toast.makeText(this,"proj ID : " + pid,Toast.LENGTH_SHORT).show();
    }
}