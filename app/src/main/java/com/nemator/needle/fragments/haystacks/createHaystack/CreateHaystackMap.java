package com.nemator.needle.fragments.haystacks.createHaystack;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.SphericalUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateHaystackMap extends SupportMapFragment implements LocationListener{
    public static final String TAG = "CreateHSMapFragment";

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = true;
    private Boolean locationUpdatesStarted = false;

    private GoogleMap mMap;
    private Marker mMarker;
    private String username = "";
    private int userId = -1;
    private Circle mCircle;
    private Polygon mPolygon;
    private Boolean cameraUpdated = false;
    private Boolean mIsPolygonCircle = true;
    private Float mScaleFactor = 1.0f;
    private LatLng mCustomPosition;
    private Boolean mUseCustomPosition = false;


    //Constructors
    public static CreateHaystackMap newInstance() {
        CreateHaystackMap fragment = new CreateHaystackMap();
        return fragment;
    }

    public CreateHaystackMap(){

    }

    //Lifecycle methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }

        //Action Bar
        setHasOptionsMenu(true);

        mGoogleApiClient = Needle.googleApiController.getGoogleApiClient();
        if(mGoogleApiClient == null){
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(apiConnectedReceiver,
                     new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
        }else{
            startLocationUpdates();
        }
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.unregisterReceiver(this);

            startLocationUpdates();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(50, 50, 50, 50);
        view.setLayoutParams(layoutParams);

        return view;
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(AppConstants.REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(AppConstants.LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(AppConstants.LOCATION_KEY);
                if(mCurrentLocation != null){
                    Double lat = mCurrentLocation.getLatitude();
                    Double lng = mCurrentLocation.getLongitude();
                    mCurrentPosition = new LatLng(lat, lng);
                }

                mCustomPosition = savedInstanceState.getParcelable(AppConstants.CUSTOM_LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(AppConstants.LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(AppConstants.LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(AppConstants.LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putParcelable(AppConstants.CUSTOM_LOCATION_KEY, mCustomPosition);
        savedInstanceState.putString(AppConstants.LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    //Google Client Methods
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AppConstants.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :

                        break;
                }
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates(){
        if(mLocationRequest == null){
            createLocationRequest();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        locationUpdatesStarted = true;
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        locationUpdatesStarted = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Double lat = mCurrentLocation.getLatitude();
        Double lng = mCurrentLocation.getLongitude();
        mCurrentPosition = new LatLng(lat, lng);

        updateMap();

        if(!cameraUpdated){
            moveCamera();
        }
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                Log.i(TAG, "Map set up");

                createLocationRequest();
            }
        }else {
            resumeOperations();
        }
    }

    public void updateMap() {
        //Update user's marker and polygon
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;
        if(position != null){
            if(mMarker == null){
                if(mIsPolygonCircle){
                    drawMarkerWithCircle(position);
                }else{
                    drawMarkerWithPolygon(position);
                }
            }else{
                if(mIsPolygonCircle){
                    updateMarkerWithCircle(position);
                }else{
                    updateMarkerWithPolygon(position);
                }
            }

            String title = (mUseCustomPosition) ? "Hasytack's Position" : "Your Position";
            mMarker.setTitle(title);
        }
    }

    private void resumeOperations(){
        //Add user's marker back
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;
        if(position != null){
            if(mMarker == null && mCircle == null){
                drawMarkerWithCircle(position);
            }else{
                updateMarkerWithCircle(position);
            }

            String title = (mUseCustomPosition) ? "Hasytack's Position" : "Your Position";
            mMarker.setTitle(title);

            updateMap();
            moveCamera();
        }

        if(!locationUpdatesStarted)
            startLocationUpdates();
    }

    public void moveCamera(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 17.0f));
        cameraUpdated = true;
    }

    public void moveCameraTo(LatLng position){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
        cameraUpdated = true;
    }

    public void moveCameraTo(LatLng position, float zoom){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        cameraUpdated = true;
    }

    public void moveUserTo(LatLng position){
        mCustomPosition = position;
        mUseCustomPosition = true;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
        updateMap();
        cameraUpdated = true;
    }

    public void moveUserTo(LatLng position, float zoom){
        mCustomPosition = position;
        mUseCustomPosition = true;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
        cameraUpdated = true;
    }

    public void moveUserToCurrentPosition(){
        mUseCustomPosition = false;
        if(mCurrentPosition != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 17.0f));
        }
    }

    public void focusCamera(float zoom){
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    //TODO : make async task ?
    public void focusCamera(){
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
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

    private void drawMarkerWithCircle(LatLng position){
        double radiusInMeters = 50.0 * mScaleFactor; //50 meters is max for now
        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        //Remove old Polygon
        if(mCircle != null){
            mCircle.remove();
        }

        if(mPolygon != null){
            mPolygon.remove();
        }

        if(mMarker != null){
            mMarker.remove();
        }

        if(mMap != null){
            CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
            mCircle = mMap.addCircle(circleOptions);

            MarkerOptions markerOptions = new MarkerOptions().position(position);
            mMarker = mMap.addMarker(markerOptions);
        }
    }

    private void updateMarkerWithCircle(LatLng position) {
        if(!mCircle.getCenter().equals(position)){
            mCircle.setCenter(position);
        }

        double radiusInMeters = 40.0 * mScaleFactor;
        mCircle.setRadius(radiusInMeters);

        if(!mMarker.getPosition().equals(position)){
            animateMarker(mMarker, position, false);
        }
    }

    private void drawMarkerWithPolygon(LatLng position){
        double sizeInMeters = 50.0 * mScaleFactor;

        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        //Remove old Polygon
        if(mCircle != null){
            mCircle.remove();
        }

        if(mPolygon != null){
            mPolygon.remove();
        }

        if(mMarker != null){
            mMarker.remove();
        }

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, sizeInMeters, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, sizeInMeters, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, sizeInMeters, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, sizeInMeters, 270));

        PolygonOptions polygonOptions = new PolygonOptions().fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8).addAll(vertices);
        mPolygon = mMap.addPolygon(polygonOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = mMap.addMarker(markerOptions);
    }

    private void updateMarkerWithPolygon(LatLng position) {
        double scale = 50.0 * mScaleFactor;

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, scale, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, scale, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, scale, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, scale, 270));

        mPolygon.setPoints(vertices);

        if(!mMarker.getPosition().equals(position)){
            animateMarker(mMarker, position, false);
        }
    }

    //Getters/Setters
    public void setScaleFactor(Float scaleFactor){
        mScaleFactor = scaleFactor;
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;

        if(mIsPolygonCircle){
            updateMarkerWithCircle(position);
        }else{
            updateMarkerWithPolygon(position);
        }
    }

    public void setIsPolygonCircle(Boolean value){
        mIsPolygonCircle = value;
        LatLng position = (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;

        if(mIsPolygonCircle){
            drawMarkerWithCircle(position);
        }else{
            drawMarkerWithPolygon(position);
        }
    }

    public int getZoneRadius(){
        return (int) Math.round(mScaleFactor * 50.0);
    }

    public LatLng getPosition(){
        return (mUseCustomPosition) ? mCustomPosition : mCurrentPosition;
    }

    public LatLngBounds getCameraTargetBounds(){
        return getMap().getProjection().getVisibleRegion().latLngBounds;
    }
}
