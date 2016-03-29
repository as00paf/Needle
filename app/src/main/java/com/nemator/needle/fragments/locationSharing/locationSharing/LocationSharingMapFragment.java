package com.nemator.needle.fragments.locationSharing.locationSharing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.broadcastReceiver.LocationServiceBroadcastReceiver;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.MarkerUtils;
import com.nemator.needle.utils.SphericalUtil;
import com.nemator.needle.views.UserMarker;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSharingMapFragment extends SupportMapFragment
        implements LocationServiceBroadcastReceiver.LocationServiceDelegate{

    public static final String TAG = "LocationSharingMap";

    //Locations
    private Location mCurrentLocation;
    private LatLng myPosition;

    private LatLng mReceivedPosition;

    //Map
    private GoogleMap mMap;
    private UserMarker myMarker, otherUsersMarker;
    private final ArrayList<Marker> markers = new ArrayList<>();

    //Map data
    private LocationSharingVO locationSharing;
    private Boolean cameraUpdated = false;

    //Location Service
    private LocationServiceBroadcastReceiver locationServiceBroadcastReceiver;
    private Boolean mRequestingLocationUpdates = true;

    //Constructors
    public static LocationSharingMapFragment newInstance() {
        LocationSharingMapFragment fragment = new LocationSharingMapFragment();
        return fragment;
    }

    public LocationSharingMapFragment(){

    }

    //Lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }else{
            locationSharing = (LocationSharingVO) ((LocationSharingActivity) getActivity()).getIntent().getExtras().get(AppConstants.TAG_LOCATION_SHARING);
        }

        //Map
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Toast.makeText(getActivity(), "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        }

        //Location Service
        Needle.serviceController.initServiceAndStartUpdates(getActivity());

        locationServiceBroadcastReceiver = new LocationServiceBroadcastReceiver(this);

        //Action Bar
        setHasOptionsMenu(true);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(AppConstants.REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(AppConstants.LOCATION_KEY);
                Double lat = mCurrentLocation.getLatitude();
                Double lng = mCurrentLocation.getLongitude();
                myPosition = new LatLng(lat, lng);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(AppConstants.LOCATION_KEY, mCurrentLocation);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        //Broadcast Receiver
        locationSharing = ((LocationSharingActivity) getActivity()).getLocationSharing();
        getActivity().registerReceiver(locationServiceBroadcastReceiver, new IntentFilter(AppConstants.LOCATION_UPDATED));

        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Needle.serviceController.stopLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(locationServiceBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_option_settings:

                return true;
            case R.id.menu_option_help:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLocationUpdated(Location location) {
        mCurrentLocation = location;
        myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        updateMap();

        if(!cameraUpdated)
            moveCamera();

        if(mRequestingLocationUpdates)
            trackUser();

        //Update distance to user
        if(myPosition != null && mReceivedPosition != null){
            double distance = SphericalUtil.computeDistanceBetween(myPosition, mReceivedPosition);
            ((LocationSharingActivity) getActivity()).updateDistance(String.valueOf(Math.round(distance)) + "m");
        }
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
        }else if(Needle.serviceController.isConnected()){
            resumeOperations();
        }
    }

    public void updateMap() {
        //Update user's marker
        if(myPosition != null){
            if(myMarker == null){
                myMarker = drawUserMarker(Needle.userModel.getUser(), myPosition);
            }else{
                myMarker.updateLocation(mMap, myPosition);
            }
        }

        //Update received user's marker
        if(mReceivedPosition != null){
            if(otherUsersMarker == null){
                UserVO user = Needle.userModel.getUserId() == locationSharing.getSender().getId() ?
                        locationSharing.getReceiver() : locationSharing.getSender();
                otherUsersMarker = drawUserMarker(user, mReceivedPosition);
            }else{
                otherUsersMarker.updateLocation(mMap, mReceivedPosition);
            }
        }
    }

    private void resumeOperations(){
        if(mMap==null)
            mMap = getMap();

        updateMap();
        moveCamera();

        if(mRequestingLocationUpdates)
            trackUser();
    }

    public void moveCamera(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 19.0f));
        cameraUpdated = true;
    }

    //Actions
    private void trackUser(){
        if(Needle.userModel.getUserId() == locationSharing.getReceiver().getId()){
            ApiClient.getInstance().retrieveSenderLocation(locationSharing.getSender().getId(), locationSharing, userLocationRetrievedCallback);
        }else if(locationSharing.isSharedBack()){
            ApiClient.getInstance().retrieveReceiverLocation(locationSharing.getReceiver().getId(), locationSharing, userLocationRetrievedCallback);
        }else{
            Log.e(TAG, "Location is not shared back. Will only be showing your location");
        }
    }

    private Callback<UserResult> userLocationRetrievedCallback = new Callback<UserResult>() {
        @Override
        public void onResponse(Call<UserResult> call, Response<UserResult> response) {
            UserResult result = response.body();
            if(result.getSuccessCode() == 1){
                mReceivedPosition = result.getUser().getLocation().getLatLng();
                updateMap();
            }else if(result.getSuccessCode() == 201 && mRequestingLocationUpdates){//LocationSharingCancelled or expired
                mRequestingLocationUpdates = false;
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.location_sharing_ended))
                        .setMessage(getString(R.string.log_out_confirmation_msg))
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getActivity().finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                getActivity().finish();
                            }
                        })
                        .show();
            }else{
                Log.e(TAG, "Failed to retrieve user's location. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<UserResult> call, Throwable t) {
            Log.e(TAG, "Failed to retrieve user's location. Error : " + t.getMessage());
        }
    };

    //Map Stuff
    private UserMarker drawUserMarker(UserVO user, LatLng position){
        double radiusInMeters = 10.0;
        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        Circle circle = mMap.addCircle(circleOptions);
        final Marker marker = MarkerUtils.createUserMarker(getActivity(), mMap, user, position).getMarker();

        String label = Needle.userModel.getUserId() == user.getId() ? "Your Location" : user.getReadableUserName() + "' Location";
        marker.setTitle(label);

        final UserMarker userMarker = new UserMarker(user, marker, circle, position);
        markers.add(marker);
        return userMarker;
    }
}
