package com.snu.muc.dogeeye.common;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.Location;

import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.model.LogEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.model.Quest;

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
    private static final double NEARBY_THRESHOLD = 200; // In Meter

    private final int currentProjectId;
    private final Context mContext;
    private final ProjectDB projectDb;
    private final ProjectDao projectDao;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public QuestChecker(Context context, int id) {
        mContext = context;
        currentProjectId = id;
        projectDb = ProjectDB.getProjectDB(context);
        projectDao = projectDb.projectDao();
        sp = context.getSharedPreferences("Quest", MODE_PRIVATE);
        editor = sp.edit();
    }

    public List<Quest> getNewlyAchievedQuests() {
        List<Quest> achieved = new ArrayList();

        achieved.addAll(checkStreakQuests());
        achieved.addAll(checkStepsQuests());
        achieved.addAll(checkLandmarkQuests());
        achieved.addAll(checkExpeditionQuests());
        achieved.addAll(checkInterestingQuests());

        // Check duplication
        List<Quest> ret = new ArrayList();
        for (Quest quest : achieved) {
            if (checkQuest(quest.getId())) {
                continue;
            }
            setUnlock(quest.getId());
            ret.add(quest);
        }
        return ret;
    }

    private List<Quest> checkInterestingQuests() {
        List<Quest> interestingQuests = new ArrayList();
        List<Project> projects = projectDao.getAllProjectsOrderedByStartTime();

        // Check To the Moon
        double totalDistance = 0;
        for (int i = 0; i < projects.size(); i++) {
            totalDistance += projects.get(i).getEveryMovingDistance();
            if (totalDistance > 100000.0f) {
                interestingQuests.add(new Quest(mContext.getString(R.string.achievement_to_the_moon),
                        "To the Moon", "Traveled 100kms in total"));
                log.d("Achieved = achievement_to_the_moon");
            }
        }

        // Check Photographer
        if (false) {
            interestingQuests.add(new Quest(mContext.getString(R.string.achievement_photographer),
                    "Photographer", "Take 100 Photos"));
            log.d("Achieved = achievement_photographer");
        }

        // Check Love Yourself
        if (false) {
            interestingQuests.add(new Quest(mContext.getString(R.string.achievement_love_yourself),
                    "Love Yourself", "Take 100 Selfies"));
            log.d("Achieved = achievement_love_yourself");
        }
        return interestingQuests;
    }

    private List<Quest> checkExpeditionQuests() {
        List<Quest> expeditionQuests = new ArrayList();
        Project current = projectDao.getProjectsByID(currentProjectId);

        if (current.getEveryMovingDistance() >= 1000f) {
            expeditionQuests.add(new Quest(mContext.getString(R.string.achievement_expedition_1km),
                    "Expedition 1km", "Walk 1km at once"));
            log.d("Achieved = achievement_expedition_1km");
        }
        if (current.getEveryMovingDistance() >= 2000f) {
            expeditionQuests.add(new Quest(mContext.getString(R.string.achievement_expedition_2km),
                    "Expedition 2km", "Walk 2km at once"));
            log.d("Achieved = achievement_expedition_2km");
        }
        if (current.getEveryMovingDistance() >= 3000f) {
            expeditionQuests.add(new Quest(mContext.getString(R.string.achievement_expedition_3km),
                    "Expedition 3km", "Walk 3km at once"));
            log.d("Achieved = achievement_expedition_3km");
        }
        if (current.getEveryMovingDistance() >= 5000f) {
            expeditionQuests.add(new Quest(mContext.getString(R.string.achievement_expedition_5km),
                    "Expedition 5km", "Walk 5km at once"));
            log.d("Achieved = achievement_expedition_5km");
        }

        return expeditionQuests;
    }

    private List<Quest> checkLandmarkQuests() {
        List<Quest> landmarkQuests = new ArrayList();
        List<LogEntity> logs = projectDao.getProjectLog(currentProjectId);

        // Check old
        double targetLa = 37.579617;
        double targetLo = 126.977041;
        for (int i = 0; i < logs.size(); i++) {
            if (calculateDistanceInMeter(logs.get(i).getLa(), logs.get(i).getLo(), targetLa, targetLo) < NEARBY_THRESHOLD) {
                landmarkQuests.add(new Quest(mContext.getString(R.string.achievement_landmarkold),
                        "Landmark-OLD", "Visit heart of chosun dynasty"));
                log.d("Achieved = achievement_landmarkold");
                break;
            }
        }

        // Check forest
        targetLa = 37.5443214;
        targetLo = 127.0374668;
        for (int i = 0; i < logs.size(); i++) {
            if (calculateDistanceInMeter(logs.get(i).getLa(), logs.get(i).getLo(), targetLa, targetLo) < NEARBY_THRESHOLD) {
                landmarkQuests.add(new Quest(mContext.getString(R.string.achievement_landmarkforest),
                        "Landmark-Forest", "Visit Seoul Forest"));
                log.d("Achieved = achievement_landmarkforest");
                break;
            }
        }

        // Check gate
        targetLa = 37.4486771;
        targetLo = 126.4517732;
        for (int i = 0; i < logs.size(); i++) {
            if (calculateDistanceInMeter(logs.get(i).getLa(), logs.get(i).getLo(), targetLa, targetLo) < NEARBY_THRESHOLD) {
                landmarkQuests.add(new Quest(mContext.getString(R.string.achievement_landmarkthe_gate),
                        "Landmark-The Gate", "Visit Inchon In't Airport"));
                log.d("Achieved = achievement_landmarkthe_gate");
                break;
            }
        }

        // Check home
        targetLa = 37.449509;
        targetLo = 126.9525151;
        for (int i = 0; i < logs.size(); i++) {
            if (calculateDistanceInMeter(logs.get(i).getLa(), logs.get(i).getLo(), targetLa, targetLo) < NEARBY_THRESHOLD) {
                landmarkQuests.add(new Quest(mContext.getString(R.string.achievement_landmarksnu),
                        "Landmark-SNU", "Visit home of this app"));
                log.d("Achieved = achievement_landmarksnu");
                break;
            }
        }
        return landmarkQuests;
    }

    private double calculateDistanceInMeter(double orgLa, double orgLo, double dstLa, double dstLo) {
        Location startLoc = new Location("start");
        startLoc.setLatitude(orgLa);
        startLoc.setLongitude(orgLo);

        Location endLoc = new Location("end");
        endLoc.setLatitude(dstLa);
        endLoc.setLongitude(dstLo);

        return startLoc.distanceTo(endLoc);
    }

    private List<Quest> checkStepsQuests() {
        List<Quest> stepQuests = new ArrayList();
        List<Project> projects = projectDao.getAllProjectsOrderedByStartTime();
        int number = 0;
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getTotalStep() > 10000.0f) {
                number++;
            }
        }

        if (number >= 10) {
            stepQuests.add(new Quest(mContext.getString(R.string.achievement_10x_10000_steps),
                    "10x 10000 Steps", "Walk more than 10000 steps for 10 times"));
            log.d("Achieved = achievement_10x_10000_steps");
        }
        if (number >= 20) {
            stepQuests.add(new Quest(mContext.getString(R.string.achievement_20x_10000_steps),
                    "10x 20000 Steps", "Walk more than 10000 steps for 20 times"));
            log.d("Achieved = achievement_20x_10000_steps");
        }
        if (number >= 30) {
            stepQuests.add(new Quest(mContext.getString(R.string.achievement_30x_10000_steps),
                    "10x 30000 Steps", "Walk more than 10000 steps for 30 times"));
            log.d("Achieved = achievement_30x_10000_steps");
        }
        return stepQuests;
    }

    private List<Quest> checkStreakQuests() {
        List<Quest> streakQuests = new ArrayList();
        Project current = projectDao.getProjectsByID(currentProjectId);
        List<Project> projects = projectDao.getAllProjectsOrderedByStartTime();

        // check 3x streak, 5x streak, 7x streak
        int maxDayStreak = getDaysStreak(current, projects);
        if (maxDayStreak >= 3) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_3x_streak),
                    "3 Days streak", "Walk 3 or more times in a week"));
            log.d("Achieved = achievement_3x_streak");
        }
        if (maxDayStreak >= 5) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_5x_streak),
                    "5 Days streak", "Walk 5 or more times in a week"));
            log.d("Achieved = achievement_5x_streak");
        }
        if (maxDayStreak >= 7) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_7x_streak),
                    "7 Days streak", "Walk 7 or more times in a week"));
            log.d("Achieved = achievement_7x_streak");
        }

        // check 3-week streak, 4-week streak, 5-week streak
        int maxWeekStreak = getWeeksStreak(current, projects);
        if (maxWeekStreak >= 3) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_3week_streak),
                    "3 Weeks streak", "Walk 3 weeks in a row"));
            log.d("Achieved = achievement_3week_streak");
        }
        if (maxWeekStreak >= 4) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_4week_streak),
                    "4 Weeks streak", "Walk 4 weeks in a row"));
            log.d("Achieved = achievement_4week_streak");
        }
        if (maxWeekStreak >= 5) {
            streakQuests.add(new Quest(mContext.getString(R.string.achievement_5week_streak),
                    "5 Weeks streak", "Walk 5 weeks in a row"));
            log.d("Achieved = achievement_5week_streak");
        }
        return streakQuests;
    }

    private boolean checkQuest(String id) {
        return sp.getBoolean(id, false);
    }

    private void setUnlock(String id) {
        editor.putBoolean(id, true);
        editor.commit();
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
