package com.snu.muc.dogeeye;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(foreignKeys = @ForeignKey(entity = Project.class,
        parentColumns = "id",
        childColumns = "PID",
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE
))
public class LogEntity {

    @PrimaryKey(autoGenerate = true)
    private int LID = 0;

    private String logTime;

    private Double La;

    private Double Lo;

    private String locName;

    private float localStep;

    private float globalStep;

    @ColumnInfo(index = true)
    private int PID;

    public int getLID() {
        return LID;
    }

    public void setLID(int LID) {
        this.LID = LID;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public Double getLa() {
        return La;
    }

    public void setLa(Double la) {
        La = la;
    }

    public Double getLo() {
        return Lo;
    }

    public void setLo(Double lo) {
        Lo = lo;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public float getLocalStep() {
        return localStep;
    }

    public void setLocalStep(float localStep) {
        this.localStep = localStep;
    }

    public float getGlobalStep() {
        return globalStep;
    }

    public void setGlobalStep(float globalStep) {
        this.globalStep = globalStep;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public void copyEntity(LogEntity entity){
        this.La = entity.getLa();
        this.Lo = entity.getLo();
        this.globalStep = entity.getGlobalStep();
        this.localStep = entity.getLocalStep();
        this.logTime = entity.getLogTime();
        this.PID = entity.getPID();
        this.LID = entity.getLID();
    }
}
