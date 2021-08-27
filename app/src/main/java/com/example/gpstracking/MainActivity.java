package com.example.gpstracking;
//https://github.com/shadsluiter/gpsDemoStarting/blob/master/activity_main.xml
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int Default_Update = 30;
    public static final int Default_Update_Interval = 30;
    public static final int Fast_Update_Interval = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat,tv_lon,tv_altitude,tv_accuracy,tv_speed,tv_sensor,tv_updates, tv_address,tv_wayPointCounts;
    Button btn_ShowWayPointList,btn_newWaypoint,btn_showMap;

    Switch sw_locationupdates , sw_gps;

    boolean updateOn = false;

    //current location
    Location currentLocation;

    //list of saved locations
    List<Location> savedLocations;

    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    //googles Api for location services
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //give each UI variable a value

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumber);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWaypoint = findViewById(R.id.btn_newWayPoint);
        btn_ShowWayPointList = findViewById(R.id.btn_ShowWayPointList);
        btn_showMap = findViewById(R.id.btn_showMap);


        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000 * Default_Update_Interval);

        locationRequest.setFastestInterval(1000 * Fast_Update_Interval);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //event that is triggered when ever the update interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                updateUIValues(locationResult.getLastLocation());
            }
        };

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the gps location

                //add the new location to the global list
                MyApplication myApplication = (MyApplication)getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });

        btn_ShowWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ShowSavedLocationsList.class);
                startActivity(i);
            }
        });
        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });


        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_gps.isChecked()){

                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                }
                else{
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("UsingTowers +WIFI");
                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_locationupdates.isChecked()){
                    //turn on local tracking
                    startLocationUpdates();
                }
                else{
                    //turn off tracking
                    stopLocationUpdates();
                }
            }
        });


        updateGPS();

    }


    private void stopLocationUpdates() {
        tv_updates.setText("location is not being tracked");
        tv_lat.setText("Not Tracking Location ");
        tv_lon.setText("Not Tracking Location");
        tv_speed.setText("Not Tracking Location");
        tv_accuracy.setText("Not Tracking Location");
        tv_altitude.setText("Not Tracking Location");
        tv_sensor.setText("Not Tracking Location");
        tv_address.setText("Not tracking location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        tv_updates.setText("location is being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this,"This app requires permission to be granted to work properly",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS(){
        //get permissions from user to track gps
        //get current location from fused client
        //update the UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission.put values at location
                    updateUIValues(location);
                    currentLocation = location;

                }
            });
        }
        else{
            //not granted permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location){
        //updates all values in text view objects about new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("Not Available");
        }
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Not Available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("unable to get street address");

        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        //show the no of waypoints saved
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
    }
}