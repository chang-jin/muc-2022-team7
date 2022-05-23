package com.snu.muc.dogeeye.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Project {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    private String startTime;

    private String endTime;

    private String address;

    private float totalStep;

    private float totalDistance;

    private float range;

    public float getTotalStep() {
        return totalStep;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTotalStep(float totalStep) {
        this.totalStep = totalStep;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

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

    public void copyProject(Project project)
    {
        this.id = project.getId();
        this.startTime = project.getStartTime();
    }
}

