package com.snu.muc.dogeeye.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Project.class,
        parentColumns = "id",
        childColumns = "PID",
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE
))
public class PhotoEntity {
    @PrimaryKey(autoGenerate = true)
    private int photoId = 0;

    private String fileName;
    private String filePath;
    private int facing;
    @ColumnInfo(index = true)
    private int PID;

    private String createdAt;

    public int getPhotoId() {
        return photoId;
    }
    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public int getFacing() {
        return facing;
    }
    public void setFacing(int facing) {
        this.facing = facing;
    }
    public int getPID() {
        return PID;
    }
    public void setPID(int PID) {
        this.PID = PID;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
