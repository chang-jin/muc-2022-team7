package com.snu.muc.dogeeye.ui.logs;

import com.snu.muc.dogeeye.model.Project;

import java.util.ArrayList;

public class logEntity {

    private int type;
    private Project project;
    private String date;

    logEntity(Project proj,String dateAxis){
        type = 0;
        project = proj;
        date = dateAxis;
    }

    logEntity(String dateAxis){
        type = 1;
        date = dateAxis;
        project = null;
    }

    int getType()
    {
        return type;
    }

    String getDate(){
        return date;
    }

    public static ArrayList<logEntity> getEntityList(ArrayList<Project> allProject)
    {
        ArrayList<logEntity> buf = new ArrayList<>();
        String prevTime = "";
        for(int i = 0 ; i < allProject.size(); ++i)
        {
            Project tmp = allProject.get(i);
            String curDate = tmp.getStartTime();
            String[] times = curDate.split(" ");

            if(!prevTime.equals(times[0]))
                buf.add(new logEntity(times[0]));

            buf.add(new logEntity(tmp,times[0]));

            prevTime = times[0];
        }

        return buf;
    }
}
