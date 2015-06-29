package com.nemator.needle.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.tasks.postLocation.PostLocationParams;
import com.nemator.needle.tasks.postLocation.PostLocationTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppUtils;

import java.text.DateFormat;
import java.util.Date;

public class NeedleLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private final IBinder mBinder = new LocalBinder();
    private Boolean servicesAvailable = false;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = true;

    private Boolean mPostingLocationUpdates = false;
    private Boolean isActivated = false;
    private Boolean locationUpdatesStarted = false;

    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    public class LocalBinder extends Binder {
        public NeedleLocationService getService() {
            return NeedleLocationService.this;
        }
    }

    public NeedleLocationService() {
    }

    @Override
    public void onCreate() {
        mInProgress = false;
        createLocationRequest();
        servicesAvailable = servicesConnected();
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
        } else {

            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;

        connectToApiClient();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        connectToApiClient();
        return mBinder;
    }

    //Google Client Methods
    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            createLocationRequest();
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {

        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(this, "Error encountered\nError # :" + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null) {
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();

            mCurrentPosition = new LatLng(lat, lng);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Get current Location
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mCurrentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        //Broadcast Intent
        Intent intent = new Intent();
        intent.setAction(AppConstants.LOCATION_UPDATED);
        intent.putExtra(AppConstants.TAG_LOCATION, mCurrentLocation);
        sendBroadcast(intent);

        //Post Location
        if(mPostingLocationUpdates)
            postLocation();

    }

    //Actions
    public void postLocation(){
        if(!mPostingLocationUpdates){
            return;
        }

        PostLocationParams params = new PostLocationParams(this, AppUtils.getUserName(this), AppUtils.getUserId(this), mCurrentLocation, mCurrentPosition, false);
        new PostLocationTask(params).execute();
    }

    //Public methods
    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        locationUpdatesStarted = true;
    }

    public void stopLocationUpdates() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        locationUpdatesStarted = false;
    }

    public void shareLocation(){
        mPostingLocationUpdates = true;
        postLocation();
    }

    public void stopSharingLocation(){
        mPostingLocationUpdates = false;
    }

    public void toggleLocationSharing(){
        if(mPostingLocationUpdates){
            stopSharingLocation();
        }else{
            shareLocation();
        }
    }

    //Getters/Setters
    public Boolean isConnected(){
        return mGoogleApiClient.isConnected();
    }

    public Boolean isPostingLocationUpdates() {
        return mPostingLocationUpdates;
    }

    public void isPostingLocationUpdates(Boolean mPostingLocationUpdates) {
        this.mPostingLocationUpdates = mPostingLocationUpdates;
    }

}
