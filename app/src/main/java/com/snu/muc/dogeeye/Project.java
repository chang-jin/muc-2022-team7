package com.snu.muc.dogeeye;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Project {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    private String startTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}

