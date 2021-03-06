package com.nemator.needle.service;

import android.Manifest;
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
import com.nemator.needle.Needle;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.models.vo.LocationVO;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.tasks.db.PostLocationRequestDBCleanupTask.PostLocationRequestDBCleanupTask;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestTask;
import com.nemator.needle.tasks.db.isPostLocationRequestDBEmpty.IsPostLocationRequestDBEmptyTask;
import com.nemator.needle.tasks.db.isPostLocationRequestDBEmpty.IsPostLocationRequestDBEmptyTask.IsPostLocationRequestDBEmptyResponseHandler;
import com.nemator.needle.tasks.db.removePostLocationRequest.RemovePostLocationRequestTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeedleLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, AddPostLocationRequestTask.AddPostLocationRequestHandler, IsPostLocationRequestDBEmptyResponseHandler{

    private static final String TAG = "NeedleService";

    //Objects
    private final IBinder mBinder = new LocalBinder();
    private GoogleApiClient mGoogleApiClient;

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
        servicesAvailable = isServicesConnected();
    }

    private boolean isServicesConnected() {
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
    public void onConnected(Bundle connectionHint){
        Log.d(TAG, "NeedleLocationService connected !");

        if (mRequestingLocationUpdates && PermissionManager.getInstance(this).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            startLocationUpdates();

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if(mCurrentLocation != null) {
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();

            mCurrentPosition = new LatLng(lat, lng);
        }

        isPostLocationRequestDBEmpty();
    }

    private void cleanUpDB(){
        new PostLocationRequestDBCleanupTask(this).execute();
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
    }

    public void isPostLocationRequestDBEmpty(){
        Log.d(TAG, "isPostLocationRequestDBEmpty");
        new IsPostLocationRequestDBEmptyTask(this, this).execute();
    }

    @Override
    public void onPostLocationRequestDBIsEmptyResult(Boolean isNotEmpty) {
        if(isNotEmpty){
            if(!mRequestingLocationUpdates){
                Log.d(TAG, "Post Location Request DB is not empty, starting to request location updates");
                startLocationUpdates();
            }else{
                Log.d(TAG, "Post Location Request DB is not empty, location updates already requested");
            }

            if(!mPostingLocationUpdates){
                Log.d(TAG, "Post Location Request DB is not empty, starting to post location updates");
                shareLocation();
            }else{
                Log.d(TAG, "Post Location Request DB is not empty, already posting location updates");
                postLocation();
            }
        }else if(!mRequestingLocationUpdates){
            Log.d(TAG, "Post location request DB is empty and not requesting location updates, stopping needle location service");
            stopLocationUpdates();
            mPostingLocationUpdates = false;
            mGoogleApiClient.disconnect();
            stopSelf();
        }else{
            mPostingLocationUpdates = false;
        }
    }

    //Actions
    public void postLocation(){
        if(!mPostingLocationUpdates || mCurrentPosition == null || mCurrentPosition == mLastUpdatedPosition ){
            return;
        }

        mLastUpdatedPosition = mCurrentPosition;

        Needle.userModel.getUser(this).setLocation(new LocationVO(mCurrentPosition));

        ApiClient.getInstance().updateLocation(Needle.userModel.getUser(), new Callback<UserResult>() {
            @Override
            public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                Log.d(TAG, "Location Updated");
            }

            @Override
            public void onFailure(Call<UserResult> call, Throwable t) {
                Log.d(TAG, "Location Update Failed");
            }
        });
    }

    //Public methods
    public void startLocationUpdates() {
        if(mGoogleApiClient.isConnected()){//TODO: don't think that's needed anymore
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }else{
            mGoogleApiClient.connect();
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
