package com.nemator.needle.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nemator.needle.service.NeedleLocationService;

/**
 * Created by Alex on 02/07/2015.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NeedleLocationService.class);
        context.startService(serviceIntent);
    }
}
