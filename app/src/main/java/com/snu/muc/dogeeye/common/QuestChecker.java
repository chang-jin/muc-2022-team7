package com.snu.muc.dogeeye.common;

import android.content.Context;

import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import java.util.ArrayList;
import java.util.List;

/**
 * User 가 하나의 Walk 를 끝냈을 때
 * Project / LogEntity DB 내용을 체크해서 새롭게 달성 가능한
 * Quest 들을 확인하는 Class
 */
public class QuestChecker {

    private final int currentProjectId;
    private final Context mContext;
    private final ProjectDB projectDb;
    private final ProjectDao projectDao;

    public QuestChecker(Context context, int id) {
        mContext = context;
        currentProjectId = id;
        projectDb = ProjectDB.getProjectDB(context);
        projectDao = projectDb.projectDao();
    }

    public List<Integer> getNewlyAchievedQuests() {
        return new ArrayList();
    }
}
