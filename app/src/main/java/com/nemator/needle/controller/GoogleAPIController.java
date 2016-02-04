package com.nemator.needle.controller;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionManager;

/**
 * Created by Alex on 11/08/2015.
 */
public class GoogleAPIController implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    private static GoogleAPIController instance;
    public final String TAG = "GoogleAPIController";

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult connectionResult;
    private HomeActivity activity;

    private boolean isConnected = false;

    private GoogleSignInOptions gso;

    private static final int SETTINGS_REQUEST_ID = 1338;

    public GoogleAPIController() {
    }

    public static GoogleAPIController getInstance() {
        if (instance == null) {
            instance = new GoogleAPIController();
        }

        return instance;
    }

    public void init(HomeActivity activity) {
        this.activity = activity;

        connect();
    }

    public void connect() {
        if (!isConnected) {
            initApiClient();
        } else {
            sendIntent(AppConstants.GOOGLE_API_CONNECTED);
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void initApiClient() {
        //Google API Client
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            Toast.makeText(activity, "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        } else {
            connectToApiClient();
        }
    }

    private void connectToApiClient() {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //Location APIs
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                        //Social APIs
                .enableAutoManage(activity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGSO())
                .build();
    }

    //Connection Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Api Connected !");

        isConnected = true;
        sendIntent(AppConstants.GOOGLE_API_CONNECTED);
    }

    private void sendIntent(String action) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        Intent intent = new Intent();
        intent.setAction(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        this.connectionResult = connectionResult;
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, AppConstants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        Toast.makeText(activity, "Error encountered\nError # :" + errorCode, Toast.LENGTH_SHORT).show();
    }

    //Getters/Setters
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public boolean isConnected() {
        return isConnected;
    }


    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }

    public void checkLocationSettings() {
        LocationRequest request = new LocationRequest()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder b = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        b.build());

        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        if (locationSettingsResult.getStatus().getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
            try {
                locationSettingsResult
                        .getStatus()
                        .startResolutionForResult(activity, SETTINGS_REQUEST_ID);
            } catch (IntentSender.SendIntentException e) {
                // oops
            }
        } else {
            isConnected = true;
            sendIntent(AppConstants.GOOGLE_API_CONNECTED);
        }

    }

    public void getCurrentPlace(ResultCallback<PlaceLikelihoodBuffer> callback) {
        if (PermissionManager.getInstance(activity).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(callback);

        }else{
            Log.d(TAG, "Permission is not granted");
        }

    }

    public GoogleSignInOptions getGSO() {
        if(gso == null){
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
        }

        return gso;
    }
}

