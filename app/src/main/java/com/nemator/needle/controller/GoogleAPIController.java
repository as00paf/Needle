package com.nemator.needle.controller;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.github.gorbin.asne.googleplus.MomentUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.nemator.needle.MainActivity;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackMapFragment;

/**
 * Created by Alex on 11/08/2015.
 */
public class GoogleAPIController implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleAPIController instance;
    public final String TAG = "GoogleAPIController";

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult connectionResult;
    private Person currentPerson;
    MainActivity activity;

    private boolean isConnected;

    public GoogleAPIController(){
    }

    public static GoogleAPIController getInstance(){
        if(instance == null){
            instance = new GoogleAPIController();
        }

        return instance;
    }

    public void init(MainActivity activity){
        this.activity = activity;
        connect();
    }

    public void connect(){
        if(!isConnected ) initApiClient();
    }

    public void disconnect(){
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void initApiClient() {
        //Google API Client
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            Toast.makeText(activity, "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        }else{
            connectToApiClient();
        }
    }

    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }else if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Plus.PlusOptions plusOptions = new Plus.PlusOptions.Builder()
                .addActivityTypes(MomentUtil.ACTIONS)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //Location APIs
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                //Social APIs
                .addApi(Plus.API, plusOptions)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    //Connection Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {
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
           /* try {
                connectionResult.startResolutionForResult(activity, AppConstants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }*/
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(activity, "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    //Getters/Setters
    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public boolean isConnected() {
        return isConnected;
    }


    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}
