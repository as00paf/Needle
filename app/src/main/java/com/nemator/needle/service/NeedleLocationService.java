package com.nemator.needle.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.addPostLocationRequest.AddPostLocationRequestTask;
import com.nemator.needle.tasks.isPostLocationRequestDBEmpty.IsPostLocationRequestDBEmptyTask;
import com.nemator.needle.tasks.isPostLocationRequestDBEmpty.IsPostLocationRequestDBEmptyTask.IsPostLocationRequestDBEmptyResponseHandler;
import com.nemator.needle.tasks.location.LocationTask;
import com.nemator.needle.tasks.location.LocationTaskParams;
import com.nemator.needle.tasks.removePostLocationRequest.RemovePostLocationRequestTask;
import com.nemator.needle.utils.AppConstants;

import java.text.DateFormat;
import java.util.Date;

public class NeedleLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, AddPostLocationRequestTask.AddPostLocationRequestHandler, IsPostLocationRequestDBEmptyResponseHandler{

    //Objects
    private final IBinder mBinder = new LocalBinder();
    private GoogleApiClient mGoogleApiClient;
    public UserModel userModel;

    //Data
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private LatLng mLastUpdatedPosition;
    private LocationRequest mLocationRequest;

    // Flags
    private Boolean mPostingLocationUpdates = false;
    private Boolean mRequestingLocationUpdates = false;
    private Boolean servicesAvailable = false;
    private boolean mInProgress;
    private boolean checkForPostRequests;

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
        return (ConnectionResult.SUCCESS == resultCode);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!servicesAvailable || (mGoogleApiClient != null &&mGoogleApiClient.isConnected()) || mInProgress)
            return START_STICKY;

        connectToApiClient();
        checkForPostRequests = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mGoogleApiClient == null || mGoogleApiClient.isConnected())
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

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null) {
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();

            mCurrentPosition = new LatLng(lat, lng);
        }

        if(checkForPostRequests){
            checkForPostRequests = false;
            isPostLocationRequestDBEmpty();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Get current Location
        mCurrentLocation = location;
        mCurrentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        //Broadcast Intent
        Intent intent = new Intent();
        intent.setAction(AppConstants.LOCATION_UPDATED);
        intent.putExtra(AppConstants.TAG_LOCATION, mCurrentLocation);
        sendBroadcast(intent);

        //Post Location
        isPostLocationRequestDBEmpty();
        postLocation();
    }

    private void isPostLocationRequestDBEmpty(){
        new IsPostLocationRequestDBEmptyTask(this, this).execute();
    }

    @Override
    public void onPostLocationRequestDBIsEmptyResult(Boolean isNotEmpty) {
        mPostingLocationUpdates = isNotEmpty;

        if(isNotEmpty){
            if(!mRequestingLocationUpdates){
                mRequestingLocationUpdates = true;
                startLocationUpdates();
            }

        }else if(!mRequestingLocationUpdates){
            stopLocationUpdates();
            mPostingLocationUpdates = false;
            stopSelf();
        }
    }

    //Actions
    public void postLocation(){
        if(!mPostingLocationUpdates || mCurrentPosition == null || mCurrentPosition == mLastUpdatedPosition ){
            return;
        }

        mLastUpdatedPosition = mCurrentPosition;

        LocationTaskParams params = new LocationTaskParams(this, LocationTaskParams.TYPE_UPDATE, String.valueOf(userModel.getUserId()), mCurrentPosition);
        new LocationTask(params).execute();
    }

    //Public methods
    public void startLocationUpdates() {
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        mRequestingLocationUpdates = true;
    }

    public void stopLocationUpdates() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        mRequestingLocationUpdates = false;
    }

    public void shareLocation(){
        mPostingLocationUpdates = true;
        postLocation();
    }

    public void stopSharingLocation(){
        mPostingLocationUpdates = false;
    }

    public void addPostLocationRequest(int type, String expiration, int posterId, String itemId){
        AddPostLocationRequestParams params = new AddPostLocationRequestParams(this, type, expiration, posterId, itemId);
        new AddPostLocationRequestTask(params, this).execute();
    }

    @Override
    public void onLocationRequestPosted(TaskResult result) {
        //FLAGS
        mPostingLocationUpdates = true;
        postLocation();
    }

    public void removePostLocationRequest(int type, String expiration, int posterId, String itemId){
        AddPostLocationRequestParams params = new AddPostLocationRequestParams(this, type, expiration, posterId, itemId);
        new RemovePostLocationRequestTask(params).execute();
    }

    //Getters/Setters
    public Boolean isConnected(){
        return mGoogleApiClient.isConnected();
    }

    public Boolean isPostingLocationUpdates() {
        return mPostingLocationUpdates;
    }
    public void setRequestingLocationUpdates(Boolean mRequestingLocationUpdates) {
        this.mRequestingLocationUpdates = mRequestingLocationUpdates;
    }
}
