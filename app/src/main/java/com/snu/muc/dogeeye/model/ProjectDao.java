package com.snu.muc.dogeeye.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert
    void addProject(Project proj);

    @Delete
    void delProject(Project proj);

    @Update
    void updProject(Project proj);

    @Query("select * from Project")
    List<Project> getAllProjects();

    @Query("select max(Project.id) from Project")
    int getCurrentPid();

    @Query("select * from Project order by startTime desc")
    List<Project> getAllProjectsOrderedByStartTime();

    @Query("select * from Project where id = :id")
    Project getProjectsByID(int id);

    @Query("select * from LogEntity where PID = :id")
    List<LogEntity> getLogByPID(int id);

    @Insert
    void addLog(LogEntity log);

    @Delete
    void delLog(LogEntity log);

    @Update
    void updLog(LogEntity log);

    @Insert
    void addPhoto(PhotoEntity photo);

    @Delete
    void delPhoto(PhotoEntity photo);

    @Update
    void updPhoto(PhotoEntity photo);

    @Query("select * from LogEntity")
    List<LogEntity> getAllLog();

    @Query("select * from LogEntity where PID = :id")
    List<LogEntity> getProjectLog(int id);

    @Query("select * from PhotoEntity where PID = :id")
    List<PhotoEntity> getPhotoEntities(int id);
}
