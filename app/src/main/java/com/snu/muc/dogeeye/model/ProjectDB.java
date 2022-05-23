package com.snu.muc.dogeeye.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LogEntity.class,Project.class}, version = 1)
public abstract class ProjectDB extends RoomDatabase {

    public abstract ProjectDao projectDao();

    private static ProjectDB projectDB;

    public static ProjectDB getProjectDB(Context context){

        if(projectDB == null){
            projectDB = Room.databaseBuilder(context,ProjectDB.class,"Project Database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return projectDB;

    }
}
