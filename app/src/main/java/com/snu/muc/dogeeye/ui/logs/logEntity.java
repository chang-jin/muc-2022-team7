package com.snu.muc.dogeeye.ui.logs;

import com.snu.muc.dogeeye.model.Project;

import java.util.ArrayList;

public class logEntity {

    private int type;
    private Project project;
    private String date;
    private String startTimeRev;

    public int getActivityNumber() {
        return activityNumber;
    }

    public void setActivityNumber(int activityNumber) {
        this.activityNumber = activityNumber;
    }

    private int activityNumber;

    logEntity(Project proj,String dateAxis, String sTime, int originalPos){
        type = 0;
        project = proj;
        date = dateAxis;
        startTimeRev = sTime;
        activityNumber = originalPos;
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

    String getStartTimeRev(){return startTimeRev;}

    Project getProject(){return project;}

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

            buf.add(new logEntity(tmp,times[0],times[1].split(":")[0]+"\n"+times[1].split(":")[1],i));

            prevTime = times[0];
        }

        return buf;
    }
}
