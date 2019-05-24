package com.example.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

 import com.example.myapplication.CurrentLocation;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class RepeatingAlarmService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,
                CurrentLocation.getCurrentLocation().getLatitude()
                        + "\n"
                        + CurrentLocation.getCurrentLocation().getLongitude()
                , Toast.LENGTH_LONG).show();
    }
}
