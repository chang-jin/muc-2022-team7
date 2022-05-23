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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getTotalStep() {
        return totalStep;
    }

    public void setTotalStep(float totalStep) {
        this.totalStep = totalStep;
    }

    public float getStart2EndDistance() {
        return start2EndDistance;
    }

    public void setStart2EndDistance(float start2EndDistance) {
        this.start2EndDistance = start2EndDistance;
    }

    public float getEveryMovingDistance() {
        return everyMovingDistance;
    }

    public void setEveryMovingDistance(float everyMovingDistance) {
        this.everyMovingDistance = everyMovingDistance;
    }

    public float getStart2MaxDistance() {
        return start2MaxDistance;
    }

    public void setStart2MaxDistance(float start2MaxDistance) {
        this.start2MaxDistance = start2MaxDistance;
    }

    private float start2EndDistance;

    private float everyMovingDistance;

    private float start2MaxDistance;



    public void copyProject(Project project)
    {
        this.id = project.getId();
        this.startTime = project.getStartTime();
    }


}

