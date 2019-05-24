package com.example.myapplication.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;


import com.example.myapplication.Config;
import com.example.myapplication.CurrentLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class LocationService extends Service implements LocationListener {


    public static final int INTERVAL = 5000; // 2 sec
    public static final int FIRST_RUN = 5000; // 2 seconds
    int REQUEST_CODE = 11223344;
    private LocationManager locationManager;
    AlarmManager alarmManager;
    static int i = 0;
    InetAddress inetAddress;
    Geocoder geocoder;
    List<Address> addresses;
    DatabaseReference myRef;
    String address = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Config.context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        initBestProvider();
        initLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(CurrentLocation.getProvider(), 1000, 1, this);
        geocoder = new Geocoder(this, Locale.getDefault());

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

                Toast.makeText(LocationService.this, "dia chi" + address, Toast.LENGTH_SHORT).show();

                DatabaseReference   databaseReference = database.getReference("locationResult");
                databaseReference.setValue(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        startService();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmManager != null) {
            Intent intent = new Intent(this, RepeatingAlarmService.class);
            alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
            Toast.makeText(this, "Service Stopped!", Toast.LENGTH_LONG).show();
        }
    }

    private void startService() {
        /*Intent intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                INTERVAL,
                pendingIntent);
        */
        Toast.makeText(this, "Service Started.", Toast.LENGTH_LONG).show();
    }

    private void initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        CurrentLocation.setCurrentLocation(locationManager.getLastKnownLocation(CurrentLocation.getProvider()));
    }

    private void initBestProvider() {
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        CurrentLocation.setProvider(locationManager.getBestProvider(criteria, true));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        i++;
        CurrentLocation.setCurrentLocation(location);


        try {
            addresses = geocoder.getFromLocation(CurrentLocation.getCurrentLocation().getLatitude(), CurrentLocation.getCurrentLocation().getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        /*String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();*/


        Toast.makeText(this, "Provider: " + address, Toast.LENGTH_LONG).show();

        new RequestTask().execute("grope.io");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", Double.toString(CurrentLocation.getCurrentLocation().getLatitude()));
                jsonObject.put("long", Double.toString(CurrentLocation.getCurrentLocation().getLongitude()));

                if (inetAddress == null) {
                    try {
                        inetAddress = InetAddress.getByName(params[0]);
                    } catch (Exception e) {
                        System.out.println("Exp=" + e);
                    }
                }
                if (inetAddress != null) {
                    DatagramSocket sock = new DatagramSocket();

                    byte[] buf = (jsonObject.toString()).getBytes();

                    DatagramPacket pack = new DatagramPacket(buf, jsonObject.toString().length(), inetAddress, 12345);

                    sock.send(pack);

                    sock.close();
                }
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
