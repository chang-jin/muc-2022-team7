package com.snu.muc.dogeeye.ui.record;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.snu.muc.dogeeye.EntityAdaptor;
import com.snu.muc.dogeeye.LogEntity;
import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.Project;
import com.snu.muc.dogeeye.ProjectDB;
import com.snu.muc.dogeeye.ProjectDao;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.databinding.FragmentRecordBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RecordFragment extends Fragment implements SensorEventListener {

    private ArrayList<Project> projectList;
    private TextView stepView;
    private TextView locView;
    private Context mContext;
    private Activity activity;
    private RecordViewModel recordViewModel;
    private FragmentRecordBinding binding;
    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    private Sensor stepDetectorSensor;
    private Geocoder g;
    private ProjectDao pDao;
    private ProjectDB pdb;
    recThread rthread;
    Button recButton;
    boolean recoding = false;
    int curProject = 0;
    float globalStep;
    float localStep;
    private double longitude, latitude;
    List<LogEntity> logs;
    private String landMark="";
    private String endTime;
    private float maxDistance;
    private float totalDistance;
    private Location startLoc, endLoc, midLoc;
    EntityAdaptor entityAdaptor;
    private PlacesClient placesClient;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int GPS_UTIL_LOCATION_RESOLUTION_REQUEST_CODE = 101;

    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 20000L;
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 10000L;

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
                String lm="";
                double startLo, startLa;
                LogEntity logEntity=logs.get(i);


                Log.d("uThread","working...");
                List<Address> address;
                try {
                    address = g.getFromLocation(logEntity.getLa(),logEntity.getLo(),10);

                    if(address!=null) {
                        if (address.size() == 0) {
                            Log.d("주소찾기 오류","주소찾기 오류");
                        } else {
                            Log.d("찾은 주소", address.get(0).getFeatureName());
                        }
                    }
                    lm = address.get(0).getFeatureName();
                    LogEntity newLog = new LogEntity();
                    newLog.copyEntity(logEntity);
                    newLog.setLocName(lm);
                    pDao.updLog(newLog);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(i == 0){
                    landMark+="_"+lm;
                    startLoc = new Location("start");
                    startLoc.setLatitude(logEntity.getLa());
                    startLoc.setLongitude(logEntity.getLo());
                }
                else if(i == logs.size() -1){
                    endTime = logEntity.getLogTime();
                    landMark+="_"+lm;
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
            newProject.setTotalDistance(totalDistance);
            newProject.setRange(maxDistance);
            newProject.setAddress(landMark);
            try {
                newProject.setTotalStep(logs.get(logs.size()-1).getLocalStep());
            }
            catch (Exception e){
                newProject.setTotalStep(1.0f);
            }


            pDao.updProject(newProject);

            projectList = (ArrayList<Project>) pDao.getAllProjects();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            entityAdaptor.setLogList(projectList);
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
                    LogEntity lg = new LogEntity();
                    long mNow = System.currentTimeMillis();
                    Date mDate = new Date(mNow);
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    lg.setPID(curProject);
                    lg.setLa(latitude);
                    lg.setLo(longitude);
                    lg.setGlobalStep(globalStep);
                    lg.setLocalStep(localStep);
                    lg.setLogTime(mFormat.format(mDate));

                    pDao.addLog(lg);
                }
                else
                {
                    break;
                }
            }
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            longitude = locationResult.getLastLocation().getLongitude();
            latitude = locationResult.getLastLocation().getLatitude();
            locView.setText(longitude + ", " + latitude);

//            Toast.makeText(getContext(),"LOC is called!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof Activity)
            activity = (Activity) context;
    }

    private void checkLocationSetting() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
        locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);

        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), GPS_UTIL_LOCATION_RESOLUTION_REQUEST_CODE);
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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordViewModel =
                new ViewModelProvider(this).get(RecordViewModel.class);

        binding = FragmentRecordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
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

        g = new Geocoder(getContext());

        stepView = binding.textView;
        locView = binding.textView2;
        recButton = binding.button;

        recButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (recoding) {
                    recoding = false;
                    updateThread uthread = new updateThread();
                    uthread.start();
                    try {
                        uthread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    entityAdaptor.notifyDataSetChanged();
//                    Toast.makeText(getContext(),"Recording End",Toast.LENGTH_SHORT).show();
                }
                else {
                    localStep = 0;
                    Project proj = new Project();
                    long mNow = System.currentTimeMillis();
                    Date mDate = new Date(mNow);
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    proj.setStartTime(mFormat.format(mDate));

                    pDao.addProject(proj);
                    curProject = pDao.getAllProjects().size();

//                    Toast.makeText(getContext(),"Recording Start",Toast.LENGTH_SHORT).show();

                    recoding = true;

                    rthread = new recThread();
                    rthread.start();
                }
            }
        });

        checkLocationSetting();

        pdb = ProjectDB.getProjectDB(getActivity());

        pDao = pdb.projectDao();
        projectList = (ArrayList<Project>) pDao.getAllProjects();

        RecyclerView recyclerView = binding.rv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        entityAdaptor = new EntityAdaptor(projectList);
        recyclerView.setAdapter(entityAdaptor);


        // Construct a PlacesClient
        Places.initialize(mContext.getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(activity);
        checkCurrentPlace();

        return root;
    }

    private void checkCurrentPlace() {
        Log.d(TAG, "checkCurrentPlace");
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

//        Toast.makeText(getContext(),"Step Update!",Toast.LENGTH_SHORT).show();
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            globalStep = sensorEvent.values[0];

            stepView.setText("Step : " + globalStep);
        }
        else if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            localStep += sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}