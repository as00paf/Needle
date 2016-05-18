package com.nemator.needle.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.nemator.needle.utils.AppConstants;

public class LocationServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "LocationServiceReceiver";

    private LocationServiceDelegate  mDelegate;

    public LocationServiceBroadcastReceiver(){}

    public LocationServiceBroadcastReceiver(LocationServiceDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are getting the right intent
        if( AppConstants.LOCATION_UPDATED.equals(intent.getAction())) {
            try {
                Location location = intent.getParcelableExtra(AppConstants.TAG_LOCATION);
                mDelegate.onLocationUpdated(location);
            }catch (Exception ex){
                Log.e(TAG, "Error : " + ex.getMessage());
            }
        }
    }

    public interface LocationServiceDelegate{
        void onLocationUpdated(Location location);
    }
}
