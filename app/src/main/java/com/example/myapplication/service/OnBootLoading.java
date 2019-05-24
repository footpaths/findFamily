package com.example.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class OnBootLoading extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, LocationService.class);
            context.startService(serviceLauncher);
            Toast.makeText(context, "Service loaded while device boot.", Toast.LENGTH_LONG).show();
        }
    }
}
