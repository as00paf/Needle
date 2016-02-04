package com.nemator.needle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nemator.needle.activities.HomeActivity;

/**
 * Created by Alex on 14/09/2015.
 */
public class NetworkController extends BroadcastReceiver {

    private static final String TAG = "NetworkController";
    private static NetworkController instance;

    private HomeActivity activity;
    private LocalBroadcastManager localBroadcastManager;
    private Boolean isNetworkConnected;

    public NetworkController(){
    }

    public void init(HomeActivity activity){
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
        boolean noConnectivity = intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        isNetworkConnected = !noConnectivity;

        Log.d(TAG, "Network Status Changed, Connection available : " + isNetworkConnected);
    }

    public void unregister(){
        activity.unregisterReceiver(this);
    }

    public static NetworkController getInstance() {
        if(instance == null){
            instance = new NetworkController();
        }

        return instance;
    }
}
