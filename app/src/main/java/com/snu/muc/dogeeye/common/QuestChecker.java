package com.snu.muc.dogeeye.common;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User 가 하나의 Walk 를 끝냈을 때
 * Project / LogEntity DB 내용을 체크해서 새롭게 달성 가능한
 * Quest 들을 확인하는 Class
 */
public class QuestChecker {

    private static final Logger log = new Logger();

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
        List<Integer> ret = new ArrayList();

        ret.addAll(checkStreakQuests());

        return ret;
    }

    private List<Integer> checkStreakQuests() {
        List<Integer> streakQuests = new ArrayList();
        Project current = projectDao.getProjectsByID(currentProjectId);
        List<Project> projects = projectDao.getAllProjectsOrderedByStartTime();

        // 2. check 3x streak, 5x streak, 7x streak
        int maxDayStreak = getDaysStreak(current, projects);
        if (maxDayStreak >= 3) {
            streakQuests.add(0);
        }
        if (maxDayStreak >= 5) {
            streakQuests.add(1);
        }
        if (maxDayStreak >= 7) {
            streakQuests.add(2);
        }

        // 3. check 3-week streak, 4-week streak, 5-week streak
        int maxWeekStreak = getWeeksStreak(current, projects);
        if (maxWeekStreak >= 3) {
            streakQuests.add(3);
        }
        if (maxWeekStreak >= 4) {
            streakQuests.add(4);
        }
        if (maxWeekStreak >= 5) {
            streakQuests.add(5);
        }

        // 4. Remove already achieved ones

        // 5. return the list
        return streakQuests;
    }

    private int daysBetween(Project origin, Project target) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        int ret = -1;
        try {
            DateTime day1 = new DateTime(dateFormat.parse(origin.getStartTime()));
            DateTime day2 = new DateTime(dateFormat.parse(target.getStartTime()));
            Days days = Days.daysBetween(day1, day2);
            ret = days.getDays();
            if (day1.isAfter(day2)) ret *= -1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private int weeksBetween(Project origin, Project target) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        int ret = -1;
        try {
            DateTime day1 = new DateTime(dateFormat.parse(origin.getStartTime()));
            DateTime day2 = new DateTime(dateFormat.parse(target.getStartTime()));
            Days days = Days.daysBetween(day1, day2);
            ret = days.getDays() / 7;
            if (day1.isAfter(day2)) ret *= -1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private int getDaysStreak(Project current, List<Project> projects) {
        AtomicBoolean isStreak = new AtomicBoolean(false);
        int daysOnStreak = 1;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == current.getId()) {
                isStreak.set(true);
                continue;
            }
            if (isStreak.get()) {
                if (daysBetween(current, projects.get(i)) == daysOnStreak - 1) {
                    continue;
                } else if (daysBetween(current, projects.get(i)) == daysOnStreak) {
                    daysOnStreak++;
                } else {
                    isStreak.set(false);
                    break;
                }
            }
        }
        log.d("Total day streak number = " + daysOnStreak);
        return daysOnStreak;
    }

    private int getWeeksStreak(Project current, List<Project> projects) {
        AtomicBoolean isStreak = new AtomicBoolean(false);
        int weeksOnStreak = 1;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == current.getId()) {
                isStreak.set(true);
                continue;
            }
            if (isStreak.get()) {
                if (weeksBetween(current, projects.get(i)) == weeksOnStreak - 1) {
                    continue;
                } else if (weeksBetween(current, projects.get(i)) == weeksOnStreak) {
                    weeksOnStreak++;
                } else {
                    isStreak.set(false);
                    break;
                }
            }
        }
        log.d("Total week streak number = " + weeksOnStreak);
        return weeksOnStreak;
    }
}
