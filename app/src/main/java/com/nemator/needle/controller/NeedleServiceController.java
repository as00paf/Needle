package com.nemator.needle.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.nemator.needle.Needle;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestTask;
import com.nemator.needle.tasks.db.removePostLocationRequest.RemovePostLocationRequestTask;

/**
 * Created by Alex on 20/02/2016.
 */
public class NeedleServiceController {

    //Service
    private ServiceConnection mConnection;
    private NeedleLocationService locationService;

    private Activity activity;
    private static NeedleServiceController instance;

    private boolean isConnected = false;

    public static NeedleServiceController getInstance() {
        if(instance == null){
            instance = new NeedleServiceController();
        }
        return instance;
    }

    public void initService(Activity activity) {
        initService(activity, false);
    }


    public void initServiceAndStartUpdates(Activity activity) {
        initService(activity, true);
    }

    private void initService(Activity activity, final Boolean startUpdates) {
        this.activity = activity;

        //Needle Location Service
        mConnection = new ServiceConnection(){
            public void onServiceConnected(ComponentName className, IBinder service) {
                locationService = ((NeedleLocationService.LocalBinder)service).getService();
                isConnected = true;

                if(startUpdates){
                    locationService.startLocationUpdates();
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                locationService = null;
                isConnected = false;
            }
        };

        Intent serviceIntent = new Intent(activity, NeedleLocationService.class);
        activity.startService(serviceIntent);
        activity.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void unbindService() {
        if(isConnected){
            try{
                activity.unbindService(mConnection);
            }catch (Error e){

            }
            isConnected = false;
        }
    }

    public NeedleLocationService getService() {
        return locationService;
    }

    //Actions
    public void startLocationUpdates() {
        locationService.startLocationUpdates();
    }

    public void stopLocationUpdates() {
        locationService.stopLocationUpdates();
    }

    public void addPostLocationRequest(Context context, int type, String expiration, int posterId, String itemId){
        AddPostLocationRequestParams params = new AddPostLocationRequestParams(context, type, expiration, posterId, itemId);
        new AddPostLocationRequestTask(params, null).execute();
    }

    public void removePostLocationRequest(Context context, int type, String expiration, int posterId, String itemId){
        AddPostLocationRequestParams params = new AddPostLocationRequestParams(context, type, expiration, posterId, itemId);
        new RemovePostLocationRequestTask(params).execute();
    }
}
