package com.snu.muc.dogeeye.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.model.LogEntity;
import com.snu.muc.dogeeye.model.Project;
import com.snu.muc.dogeeye.model.ProjectDB;
import com.snu.muc.dogeeye.model.ProjectDao;
import com.snu.muc.dogeeye.R;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity implements SensorEventListener {

    private Geocoder g;
    private ProjectDao pDao;
    private ProjectDB pdb;
    int curProject = 0;

    Boolean recoding = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    //UI components
    Button finish;
    Button steps;
    Button distance;
    Button takePhotoButton;
    Button takeSelfieButton;

    //service thread
    Thread rThread, uThread;

    //services
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    //service flag
    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 10000L; // loc interval : 10s
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 5000L; // loc fast interval : 5s
    private static final int GPS_UTIL_LOCATION_RESOLUTION_REQUEST_CODE = 101;


    //for step seensors
    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    private Sensor stepDetectorSensor;


    //updateThread global values
    List<LogEntity> logs;
    private float maxDistance, totalDistance;
    private String landMark="",endTime;
    private Location startLoc, endLoc, midLoc;

    //recThread global values
    private double curLongitude, curLatitude;
    private double prevLongitude, prevLatitude;
    private float globalStep, localStep;
    private float movingDistanceSum;

    final String REGEX = "[0-9]+";


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            globalStep = sensorEvent.values[0];
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            localStep += sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            prevLatitude = curLatitude;
            prevLongitude = curLongitude;
            curLongitude = locationResult.getLastLocation().getLongitude();
            curLatitude = locationResult.getLastLocation().getLatitude();
        }
    };

    private void checkLocationSetting() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
        locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RecordActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(RecordActivity.this, GPS_UTIL_LOCATION_RESOLUTION_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.w(TAG, "unable to start resolution for result due to " + sie.getLocalizedMessage());
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                        }
                    }
                });
    }

    private class updateThread extends Thread{
        @Override
        public void run() {
            Log.d("uThread","Start");
            logs = pDao.getProjectLog(curProject);
            Project project = pDao.getProjectsByID(curProject);

            maxDistance=0;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(int i = 0 ; i < logs.size() ; ++i){
                String entityLocationName="";
//                double startLo, startLa;
                LogEntity logEntity=logs.get(i);


                Log.d("uThread","working...");
                List<Address> address;
                try {
                    address = g.getFromLocation(logEntity.getLa(),logEntity.getLo(),10);

                    if(address!=null) {
                        if (address.size() == 0) {
                            Log.d("주소찾기 오류","주소찾기 오류");
                        } else {
                            Log.d("findAddr", address.get(0).toString());

                            entityLocationName = address.get(0).getFeatureName();

                            entityLocationName = entityLocationName.replaceAll("-","");
                            if(entityLocationName.matches(REGEX))
                                entityLocationName = address.get(0).getThoroughfare();

                            LogEntity newLog = new LogEntity();
                            newLog.copyEntity(logEntity);
                            newLog.setLocName(entityLocationName);
                            pDao.updLog(newLog);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(i == 0){
                    landMark+="_"+entityLocationName;
                    startLoc = new Location("start");
                    startLoc.setLatitude(logEntity.getLa());
                    startLoc.setLongitude(logEntity.getLo());
                }
                else if(i == logs.size() -1){
                    endTime = logEntity.getLogTime();
                    landMark+="_"+entityLocationName;
                    endLoc = new Location("end");
                    endLoc.setLatitude(logEntity.getLa());
                    endLoc.setLongitude(logEntity.getLo());
                    totalDistance = startLoc.distanceTo(endLoc);
                }
                else
                {
                    midLoc = new Location("mid");
                    midLoc.setLatitude(logEntity.getLa());
                    midLoc.setLongitude(logEntity.getLo());
                    float tmp = startLoc.distanceTo(midLoc);
                    if(tmp > maxDistance)
                        maxDistance = tmp;
                }
            }

            Project newProject = new Project();
            newProject.copyProject(project);
            newProject.setEndTime(endTime);
            newProject.setStart2EndDistance(totalDistance);
            newProject.setStart2MaxDistance(maxDistance);
            newProject.setAddress(landMark);
            newProject.setEveryMovingDistance(movingDistanceSum);
            try {
                newProject.setTotalStep(logs.get(logs.size()-1).getLocalStep());
            }
            catch (Exception e){
                newProject.setTotalStep(1.0f);
            }
            pDao.updProject(newProject);
        }
    }

    private class recThread extends Thread{
        @Override
        public void run(){
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (recoding) {

                    //pass initial wrong values
                    if(curLatitude == 0 || curLongitude == 0)
                        continue;

                    LogEntity lg = new LogEntity();
                    long mNow = System.currentTimeMillis();
                    Date mDate = new Date(mNow);
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    lg.setPID(curProject);
                    lg.setLa(curLatitude);
                    lg.setLo(curLongitude);
                    lg.setGlobalStep(globalStep);
                    lg.setLocalStep(localStep);
                    lg.setLogTime(mFormat.format(mDate));
                    pDao.addLog(lg);

                    Location prevLoc = new Location("prevLoc");
                    if(prevLatitude == 0 || prevLongitude == 0){
                        prevLoc.setLatitude(curLatitude);
                        prevLoc.setLongitude(curLongitude);
                    }
                    else{
                        prevLoc.setLatitude(prevLatitude);
                        prevLoc.setLongitude(prevLongitude);
                    }

                    Location curLoc = new Location("curLoc");
                    curLoc.setLatitude(curLatitude);
                    curLoc.setLongitude(curLongitude);

                    movingDistanceSum += prevLoc.distanceTo(curLoc);

                    steps.setText( Math.round(localStep) + " Steps");
                    distance.setText(movingDistanceSum + " m");

                }
                else
                {
                    return;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start recThread
        localStep = 0;
        movingDistanceSum = 0;
        Project proj = new Project();
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        proj.setStartTime(mFormat.format(mDate));
        pDao.addProject(proj);
        curProject = pDao.getCurrentPid();
        recoding = true;
        rThread = new recThread();
        rThread.start();
        Toast.makeText(this,"recThread Start!",Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        finish = findViewById(R.id.finish);
        steps = findViewById(R.id.totalStep);
        distance = findViewById(R.id.totalDistance);
        takePhotoButton = findViewById(R.id.takePhoto);
        takeSelfieButton = findViewById(R.id.takeSelfie);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "stopThread", Toast.LENGTH_LONG).show();
                recoding = false;
                uThread = new updateThread();
                uThread.start();
                try {
                    uThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(RecordActivity.this, FinishActivity.class);
                intent.putExtra("currentProjectId", curProject);
                startActivity(intent);
//                recordEndBottomSheet bottomSheet = new recordEndBottomSheet();
//                bottomSheet.show(getSupportFragmentManager(),"recording end");
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePhotoIntent = new Intent(RecordActivity.this, PhotoActivity.class);
                takePhotoIntent.putExtra("facing", 1);
                takePhotoIntent.putExtra("currentProjectId", curProject);
                startActivity(takePhotoIntent);
            }
        });

        takeSelfieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takeSelfieIntent = new Intent(RecordActivity.this, PhotoActivity.class);
                takeSelfieIntent.putExtra("facing", 0);
                takeSelfieIntent.putExtra("currentProjectId", curProject);
                startActivity(takeSelfieIntent);
            }
        });


        //get DB
        pdb = ProjectDB.getProjectDB(this);
        pDao = pdb.projectDao();

        // start getting GPS
        checkLocationSetting();

        //start getting step#
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepCountSensor == null) {
//            Toast.makeText(getContext(), "No Step Counter Sensor", Toast.LENGTH_SHORT).show();
        }
        else{
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (stepDetectorSensor == null) {
//            Toast.makeText(getContext(), "No Step Detector Sensor", Toast.LENGTH_SHORT).show();
        }
        else{
            sensorManager.registerListener(this,stepDetectorSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }

        g = new Geocoder(this);


    }
}
