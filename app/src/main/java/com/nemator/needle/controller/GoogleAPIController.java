package com.nemator.needle.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.Plus;
import com.nemator.needle.adapter.PlacesRecyclerAdapter;
import com.nemator.needle.models.vo.CustomPlace;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class GoogleAPIController implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    private static GoogleAPIController instance;
    public final String TAG = "GoogleAPIController";

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult connectionResult;
    private AppCompatActivity activity;

    private boolean isConnected = false;

    private GoogleSignInOptions gso;

    private static final int SETTINGS_REQUEST_ID = 1338;
    private LatLng lastKnownLocation;

    public GoogleAPIController() {
    }

    public static GoogleAPIController getInstance() {
        if (instance == null) {
            instance = new GoogleAPIController();
        }

        return instance;
    }

    public void init(AppCompatActivity activity) {
        disconnect();
        this.activity = activity;

        connect();
    }

    private void connect() {
        Log.i(TAG, "connect()");
        if (!isConnected || !mGoogleApiClient.isConnected()) {
            isConnected = false;
            connectToApiClient();
        } else {
            sendIntent(AppConstants.GOOGLE_API_CONNECTED);
        }
    }

    public void disconnect() {
        Log.w(TAG, "disconnect()");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        isConnected = false;
        activity = null;
    }

    public void stopAutoManage() {
        Log.w(TAG, "stopAutoManage()");
        if (mGoogleApiClient != null) {
            if(activity != null){
                mGoogleApiClient.stopAutoManage(activity);
            }
            disconnect();
        }
    }

    private void connectToApiClient() {
        Log.i(TAG, "Connecting to ApiClient");
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //Location APIs
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                        //Social APIs
                .enableAutoManage(activity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGSO())
                .addApi(Plus.API)
                .build();
    }

    //Connection Callbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Api Connected !");

        isConnected = true;

        if(activity != null){
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);

                if(lastLocation != null){
                    lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }

            sendIntent(AppConstants.GOOGLE_API_CONNECTED);
        }
    }

    private void sendIntent(String action) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        Intent intent = new Intent();
        intent.setAction(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Api Disconnected !");
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
        return (isConnected && mGoogleApiClient.isConnected());
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

            }
        } else {
            isConnected = true;
            sendIntent(AppConstants.GOOGLE_API_CONNECTED);
        }
    }

    public void getCurrentPlace(ResultCallback<PlaceLikelihoodBuffer> callback, Context context) {
        if (PermissionManager.getInstance(context).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            Log.e(TAG, "Permission is not granted");
        }

    }

    /*public ArrayList<CustomPlace> getPredictions(CharSequence constraint, PlacesRecyclerAdapter adapter, LatLngBounds bounds, AutocompleteFilter placeFilter, Context context) {
        if (isConnected()) {
            Log.i(TAG, "Executing autocomplete query for: " + constraint);
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    bounds, placeFilter);
            // Wait for predictions, set the timeout.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());

                Toast.makeText(context, "Error: " + status.toString(),
                        Toast.LENGTH_SHORT).show();

                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                CustomPlace place = new CustomPlace(prediction.getPlaceId(), prediction.getDescription(),
                        prediction.getFullText(CharacterStyle.wrap(c)), null);

                resultList.add(place);
            }
            // Buffer release
            DataBufferUtils.freezeAndClose(autocompletePredictions);

            if(adapter != null){
                adapter.notifyDataSetChanged();
            }

            return resultList;
        }else{
            Log.e(TAG, "Google API client is not connected.");
        }

        return null;
    }*/


    public GoogleSignInOptions getGSO() {
        if(gso == null){
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
        }

        return gso;
    }

    public LatLng getLastKnownLocation() {
        return lastKnownLocation;
    }
}

