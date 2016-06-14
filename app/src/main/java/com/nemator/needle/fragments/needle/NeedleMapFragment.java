package com.nemator.needle.fragments.needle;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HelpSupportActivity;
import com.nemator.needle.activities.NeedleActivity;
import com.nemator.needle.activities.SettingsActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.broadcastReceiver.LocationServiceBroadcastReceiver;
import com.nemator.needle.controller.GoogleMapCameraControllerConfig;
import com.nemator.needle.controller.GoogleMapController;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppUtils;
import com.nemator.needle.utils.SphericalUtil;
import com.nemator.needle.views.UserMarker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeedleMapFragment extends SupportMapFragment
        implements LocationServiceBroadcastReceiver.LocationServiceDelegate{

    public static final String TAG = "LocationSharingMap";

    //Locations
    private Location mCurrentLocation;
    private LatLng myPosition;

    private LatLng mReceivedPosition;

    //Map
    private UserMarker myMarker, otherUsersMarker;
    private GoogleMapController mapController;
    private GoogleMapCameraControllerConfig config;

    //Map data
    private NeedleVO needle;
    private Boolean cameraUpdated = false;

    //Location Service
    private LocationServiceBroadcastReceiver locationServiceBroadcastReceiver;
    private Boolean mRequestingLocationUpdates = true;
    private boolean followUser = false;
    private boolean focusOnMarkers = false;
    private PendingIntent standaloneTrackIntent;

    //Constructors
    public static NeedleMapFragment newInstance() {
        NeedleMapFragment fragment = new NeedleMapFragment();
        return fragment;
    }

    public NeedleMapFragment(){

    }

    //Lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }else{
            needle = (NeedleVO)  getActivity().getIntent().getExtras().get(AppConstants.TAG_LOCATION_SHARING);
        }

        //Broadcast Receiver
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

        needle = ((NeedleActivity) getActivity()).getNeedle();;
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
        needle = ((NeedleActivity) getActivity()).getNeedle();
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

        if(standaloneTrackIntent != null){
            standaloneTrackIntent.cancel();
        }

        getActivity().unregisterReceiver(locationServiceBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_option_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            case R.id.menu_option_help:
                startActivity(new Intent(getContext(), HelpSupportActivity.class));
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
            ((NeedleActivity) getActivity()).updateDistance(AppUtils.formatDistance((float) distance));
        }
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mapController == null) {
            config = new GoogleMapCameraControllerConfig().setMyLocationEnabled(false);
            mapController = new GoogleMapController(getContext(), config, new GoogleMapController.GoogleMapCallback() {
                @Override
                public void onMapInitialized(GoogleMap googleMap) {
                    resumeOperations();
                }
            }, markerClickListener);
            mapController.initMap(this);
        }else if(Needle.serviceController.isConnected()){
            resumeOperations();
        }
    }

    public void updateMap() {
        //Update user's marker
        if(myPosition != null){
            if(myMarker == null){
                myMarker = mapController.createUserMarker(getContext(), Needle.userModel.getUser(), myPosition, "Your Position");
            }else{
                mapController.updateMarkersLocation(myMarker, myPosition);
            }
        }

        //Update received user's marker
        if(mReceivedPosition != null){

            if(otherUsersMarker == null){
                UserVO user = Needle.userModel.getUserId() == needle.getSender().getId() ?
                        needle.getReceiver() : needle.getSender();
                otherUsersMarker = mapController.createUserMarker(getContext(), user, mReceivedPosition, user.getReadableUserName() + "'s Position",
                        ContextCompat.getColor(getContext(), R.color.primary_dark),
                        ContextCompat.getColor(getContext(), R.color.circleColor));
            }else{
                mapController.updateMarkersLocation(otherUsersMarker, mReceivedPosition);
            }

            if(followUser){
                if(otherUsersMarker != null){
                    mapController.cameraController.focus(otherUsersMarker.getLocation(), false);
                }
            }else if(focusOnMarkers){
                if(otherUsersMarker != null){
                    mapController.cameraController.focusOnMarkers(otherUsersMarker, myMarker);
                }else{
                    mapController.cameraController.focusOnMarkers(myMarker);
                }
            }

            if(!cameraUpdated)
                moveCamera();
        }
    }

    private void resumeOperations(){
        updateMap();
        moveCamera();

        if(mRequestingLocationUpdates){
            LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }else{
                trackUser();
            }
        }
    }

    //Move to app utils
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        startStandaloneTracking();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void startStandaloneTracking() {
        trackUser();
        addStandaloneAlarm();
    }

    private void addStandaloneAlarm(){
        Log.d(TAG, "Adding alarm for standalone tracking");
        Intent intent = new Intent(getActivity(), StandaloneUserTrackerReceiver.class);

        long scTime = 30000;//30 sec

        standaloneTrackIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scTime, standaloneTrackIntent);

    }

    public class StandaloneUserTrackerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Standalone tracking");
            addStandaloneAlarm();
            trackUser();
        }
    }

    public void moveCamera(){
        if(mReceivedPosition == null){
            mapController.cameraController.focusOnMyPosition();

            Boolean isSender = Needle.userModel.getUserId() == needle.getSender().getId();
            if(isSender && !needle.isSharedBack()){
                cameraUpdated = true;
            }
        }else{
            zoomOnMarkers(false);
            cameraUpdated = true;
        }
    }

    public void zoomOnMarkers(boolean focus) {
        followUser = false;
        focusOnMarkers = focus;

        if(otherUsersMarker != null){
            if(myMarker != null){
                mapController.cameraController.focusOnMarkers(myMarker, otherUsersMarker);
            }else{
                mapController.cameraController.focusOnMarkers(otherUsersMarker);
            }
        }
    }

    //Actions
    private void trackUser(){
        if(Needle.userModel.getUserId() == needle.getReceiver().getId()){
            ApiClient.getInstance().retrieveSenderLocation(needle.getSender().getId(), needle, userLocationRetrievedCallback);
        }else if(needle.isSharedBack()){
            ApiClient.getInstance().retrieveReceiverLocation(needle.getReceiver().getId(), needle, userLocationRetrievedCallback);
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

                int titleRes, msgRes;
                if(AppUtils.isDateAfterNow(needle.getTimeLimit(), "yyyy-MM-dd hh:mm")){
                    titleRes = R.string.needle_expired;
                    msgRes = R.string.needle_expired_msg;
                }else{
                    titleRes = R.string.needle_cancelled;
                    msgRes = R.string.needle_cancelled_msg;
                }

                UserVO otherUser;
                if(Needle.userModel.getUserId() == needle.getSender().getId()){
                    otherUser = needle.getReceiver();
                }else{
                    otherUser = needle.getSender();
                }

                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(titleRes)
                        .setMessage(getString(msgRes, otherUser.getReadableUserName()))
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

    public GoogleMapController getMapController() {
        return mapController;
    }

    public void toggleFollowUser(View button) {
        focusOnMarkers = false;
        followUser = !followUser;

        if(followUser){
            if(otherUsersMarker != null){
                mapController.cameraController.focus(otherUsersMarker.getLocation(), false);
            }
            ((ImageButton) button).setImageResource(R.drawable.ic_follow_user_off);
        }else {
            if(myMarker != null && otherUsersMarker != null){
                mapController.cameraController.focusOnMarkers(myMarker, otherUsersMarker);
            }
            ((ImageButton) button).setImageResource(R.drawable.ic_follow_user);
        }
    }

    public void focusOnMyPosition(){
        followUser = false;
        focusOnMarkers = false;

        if(myPosition != null){
            mapController.cameraController.focus(myPosition, false);
        }
    }

    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if(!marker.equals(myMarker.getMarker())){
                ((NeedleActivity) getActivity()).showInfoWindow(marker.getPosition(), otherUsersMarker.getUser());

                mapController.cameraController.focus(SphericalUtil.computeOffset(marker.getPosition(), 300, 180), false);
            }else{
                mapController.cameraController.focus(marker.getPosition(), false);
            }

            return false;
        }
    };
}
