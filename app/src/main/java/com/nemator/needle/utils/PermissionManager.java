package com.nemator.needle.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.nemator.needle.R;

/**
 * Created by Alex on 20/11/2015.
 */
public class PermissionManager {

    public static final String TAG = "PermissionManager";

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 53;
    public static final int PERMISSIONS_REQUEST_ACCESS_ACCOUNTS = 8;
    public static final String LOCATION_PERMISSION_GRANTED = "locationPermissionGranted";

    private Boolean locationPermissionGranted = false;

    private static PermissionManager instance;

    private Context context;
    private Activity activity;

    public PermissionManager(Context context) {
        super();
        this.context = context;
    }

    public static PermissionManager getInstance(Context context) {
        if(instance == null){
            instance = new PermissionManager(context);
        }
        return instance;
    }

    public Boolean isLocationPermissionGranted() {
        if(!locationPermissionGranted){
            locationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return locationPermissionGranted;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Log.d(TAG, "Permission " + permissions[0] + " access has been granted !");
                    locationPermissionGranted = true;
                } else {
                    Log.d(TAG, "Permission " + permissions[0] + " access has been denied !");

                    //Popup
                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.permission_denied_title))
                            .setMessage(context.getResources().getString(R.string.location_permission_denied))
                            .setCancelable(false)
                            .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).create().show();
                }

                /*if(GoogleMapController.getInstance(context).getGoogleMap() != null){
                    GoogleMapController.getInstance(context).getGoogleMap().setMyLocationEnabled(locationPermissionGranted);
                }*/
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public Boolean checkLocationPermission(Activity act) {
        activity = act;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain to the user why we need to read the location
                Log.i(TAG, "We need to read the location because");
            }

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return false;
        }

        return true;
    }

    public Boolean checkAccountsPermission(Activity act){
        Log.d(TAG, "checkAccountsPermission");

        activity = act;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.GET_ACCOUNTS)) {
                // Explain to the user why we need to read the location
                Log.i(TAG, "We need to read the accounts because");
            }

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.GET_ACCOUNTS},
                    PERMISSIONS_REQUEST_ACCESS_ACCOUNTS);

            return false;
        }

        return true;
    }
}
