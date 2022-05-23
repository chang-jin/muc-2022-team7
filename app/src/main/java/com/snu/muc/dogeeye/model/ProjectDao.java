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

    @Query("select * from Project where id = :id")
    Project getProjectsByID(int id);

    @Insert
    void addLog(LogEntity log);

    @Delete
    void delLog(LogEntity log);

    @Update
    void updLog(LogEntity log);

    @Query("select * from LogEntity")
    List<LogEntity> getAllLog();

    @Query("select * from LogEntity where PID = :id")
    List<LogEntity> getProjectLog(int id);

}
