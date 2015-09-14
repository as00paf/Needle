package com.nemator.needle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nemator.needle.MainActivity;

/**
 * Created by Alex on 14/09/2015.
 */
public class NetworkController extends BroadcastReceiver {

    private static final String TAG = "NetworkController";
    private MainActivity activity;
    private LocalBroadcastManager localBroadcastManager;
    private Boolean isNetworkConnected;

    public NetworkController(MainActivity activity){
        this.activity = activity;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(activity);

        activity.registerReceiver(this, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE));
        isNetworkConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network Status Changed");

        boolean noConnectivity = intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        isNetworkConnected = !noConnectivity;
    }
}
