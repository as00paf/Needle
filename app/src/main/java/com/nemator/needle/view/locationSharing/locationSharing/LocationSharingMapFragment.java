package com.nemator.needle.view.locationSharing.locationSharing;

import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.broadcastReceiver.LocationServiceBroadcastReceiver;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.trackUser.TrackUserParams;
import com.nemator.needle.tasks.trackUser.TrackUserResult;
import com.nemator.needle.tasks.trackUser.TrackUserTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppUtils;

public class LocationSharingMapFragment extends SupportMapFragment
        implements LocationServiceBroadcastReceiver.LocationServiceDelegate, TrackUserTask.TrackUserResponseHandler {

    public static final String TAG = "LocationSharingMap";

    //Locations
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;

    private LatLng mReceivedPosition;

    //Map
    private GoogleMap mMap;
    private Marker mMarker;
    private Circle mCircle;

    private Marker mReceivedMarker;
    private Circle mReceivedCircle;

    //Map data
    private LocationSharingVO locationSharing;
    private Boolean cameraUpdated = false;

    //Location Service
    private NeedleLocationService locationService;
    private LocationServiceBroadcastReceiver locationServiceBroadcastReceiver;
    private Boolean mRequestingLocationUpdates = true;
    private Boolean isSent;

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
        }

        //Map
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Toast.makeText(getActivity(), "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        }

        //Location Service
        locationService = ((MainActivity) getActivity()).getLocationService();
        locationService.startLocationUpdates();

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
                mCurrentPosition = new LatLng(lat, lng);
            }

            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                locationSharing = savedInstanceState.getParcelable(AppConstants.LOCATION_SHARING_DATA_KEY);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(AppConstants.LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putParcelable(AppConstants.LOCATION_SHARING_DATA_KEY, locationSharing);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        getActivity().registerReceiver(locationServiceBroadcastReceiver, new IntentFilter(AppConstants.LOCATION_UPDATED));

        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        locationService.stopLocationUpdates();
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
        mCurrentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        updateMap();

        if(!cameraUpdated)
            moveCamera();

        if(!isSent)
            trackUser();
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
        }else if(locationService.isConnected()){
            resumeOperations();
        }
    }

    public void updateMap() {
        //Update user's marker
        if(mCurrentLocation != null){
            if(mMarker == null && mCircle == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurrentPosition);
                drawMarkerWithCircle(mCurrentPosition, "Your Position");
            }else{
                updateMarkerWithCircle(mCurrentPosition);
            }
        }

        //Update received user's marker
        if(!isSent){
            if(mReceivedPosition != null){
                if(mReceivedMarker == null && mReceivedCircle == null){
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mReceivedPosition);
                    drawReceivedMarkerWithCircle(mReceivedPosition, locationSharing.getSenderName() + "'s Position");
                }else{
                    updateReceivedMarkerWithCircle(mReceivedPosition);
                }
            }
        }
    }

    private void resumeOperations(){
        if(mMap==null)
            mMap = getMap();

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        if(mMarker == null){
            markerOptions.position(mCurrentPosition);
            drawMarkerWithCircle(mCurrentPosition, "Your Position");
        }else{
            updateMarkerWithCircle(mCurrentPosition);
        }

        if(!isSent){
            //Add received user's marker back
            MarkerOptions receivedMarkerOptions = new MarkerOptions();
            if(mReceivedMarker == null){
                receivedMarkerOptions.position(mReceivedPosition);
                drawReceivedMarkerWithCircle(mReceivedPosition, locationSharing.getSenderName() + "'s Position");
            }else{
                updateReceivedMarkerWithCircle(mReceivedPosition);
            }
        }

        updateMap();
        moveCamera();

        trackUser();
    }

    public void moveCamera(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 19.0f));
        cameraUpdated = true;
    }

    //Actions
    private void trackUser(){
        TrackUserParams params = new TrackUserParams(String.valueOf(locationSharing.getSenderId()), AppUtils.getUserId(getActivity()));
        try{
            TrackUserTask task = new TrackUserTask(params, this);
            task.execute();
        }catch (Exception e){
            mReceivedPosition = null;
        }
    }

    @Override
    public void onUserTracked(TrackUserResult result) {
        if(result.successCode == 1){
            mReceivedPosition = result.location;
            updateMap();
        }
    }

    //Map Stuff
    private void drawMarkerWithCircle(LatLng position, String label){
        double radiusInMeters = 10.0;
        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = mMap.addMarker(markerOptions);

        mMarker.setTitle(label);
    }

    private void updateMarkerWithCircle(LatLng position) {
        if(!mCircle.getCenter().equals(position)){
            mCircle.setCenter(position);
        }

        if(!mMarker.getPosition().equals(position)){
            animateMarker(mMap, mMarker, position, false);
        }
    }

    private void drawReceivedMarkerWithCircle(LatLng position, String label){
        double radiusInMeters = 10.0;
        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mReceivedCircle = mMap.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
        markerOptions.icon(icon);
        mReceivedMarker = mMap.addMarker(markerOptions);

        mReceivedMarker.setTitle(label);
    }

    private void updateReceivedMarkerWithCircle(LatLng position) {
        if(!mReceivedCircle.getCenter().equals(position)){
            mReceivedCircle.setCenter(position);
        }

        if(!mReceivedMarker.getPosition().equals(position)){
            animateMarker(mMap, mReceivedMarker, position, false);
        }
    }



    private void animateMarker(final GoogleMap map, final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();

        Location startLocation = new Location("");
        startLocation.setLatitude(startLatLng.latitude);
        startLocation.setLongitude(startLatLng.longitude);

        Location endLocation = new Location("");
        endLocation.setLatitude(toPosition.latitude);
        endLocation.setLongitude(toPosition.longitude);

        float distance = startLocation.distanceTo(endLocation);

        if(distance > 1){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker) {
                            marker.setVisible(false);
                        } else {
                            marker.setVisible(true);
                        }
                    }

                }
            });
        }
    }


    //Getters/Setters
    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
    }

    public void setLocationSharing(LocationSharingVO locationSharing) {
        this.locationSharing = locationSharing;
    }
}
