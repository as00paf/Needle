package com.nemator.needle.home.createHaystack;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.nemator.needle.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.utils.SphericalUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateHaystackMap extends SupportMapFragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener{
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
    private HashMap<String, Marker> mMarkers;
    private String username = "";
    private int userId = -1;
    private Circle mCircle;
    private Polygon mPolygon;
    private Boolean cameraUpdated = false;
    private Boolean mIsPolygonCircle = true;
    private Float mScaleFactor = 1.0f;

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

        //Map
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Toast.makeText(getActivity(), "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        }

        //Action Bar
        setHasOptionsMenu(true);
    }

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
                Double lat = mCurrentLocation.getLatitude();
                Double lng = mCurrentLocation.getLongitude();
                mCurrentPosition = new LatLng(lat, lng);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            /*case R.id.menu_option_done:

                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            createLocationRequest();
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

    protected void startLocationUpdates() {
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
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        //Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null){
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();
            mCurrentPosition = new LatLng(lat, lng);

            updateMap();
            moveCamera();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), AppConstants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(getActivity(), "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Double lat = mCurrentLocation.getLatitude();
        Double lng = mCurrentLocation.getLongitude();
        mCurrentPosition = new LatLng(lat, lng);

        if(!cameraUpdated){
            moveCamera();
        }

        updateMap();
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();

            if (mMap != null) {
                Log.i(TAG, "Map set up");
                connectToApiClient();
            }
        }else if(mGoogleApiClient.isConnected()){
            resumeOperations();
        }
    }

    public void updateMap() {
        //Update user's marker
        if(mCurrentLocation != null){
            if(mMarker == null && mCircle == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurrentPosition);
                if(mIsPolygonCircle){
                    drawMarkerWithCircle(mCurrentPosition);
                }else{
                    drawMarkerWithPolygon(mCurrentPosition);
                }
            }else{
                if(mIsPolygonCircle){
                    updateMarkerWithCircle(mCurrentPosition);
                }else{
                    updateMarkerWithPolygon(mCurrentPosition);
                }
            }

            mMarker.setTitle("Your Position");
        }
    }

    private void resumeOperations(){
        if(mMap==null)
            mMap = getMap();

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        if(mMarker == null && mCircle == null){
            markerOptions.position(mCurrentPosition);
            drawMarkerWithCircle(mCurrentPosition);
        }else{
            updateMarkerWithCircle(mCurrentPosition);
        }

        mMarker.setTitle("Your Position");

        if(!locationUpdatesStarted)
            startLocationUpdates();

        updateMap();
        moveCamera();
    }

    public void moveCamera(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 17.0f));
        cameraUpdated = true;
    }

    public void focusCamera(float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, zoom));
    }

    //TODO : make async task ?
    public void focusCamera(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 17.0f));
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

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = mMap.addMarker(markerOptions);
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
    private String getUserName(){
        if(username == ""){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            username = sp.getString("username", "");
        }

        return username;
    }

    public int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    public void scaleFactor(Float scaleFactor){
        mScaleFactor = scaleFactor;

        if(mIsPolygonCircle){
            updateMarkerWithCircle(mCurrentPosition);
        }else{
            updateMarkerWithPolygon(mCurrentPosition);
        }
    }

    public void setIsPolygonCircle(Boolean value){
        mIsPolygonCircle = value;

        if(mIsPolygonCircle){
            drawMarkerWithCircle(mCurrentPosition);
        }else{
            drawMarkerWithPolygon(mCurrentPosition);
        }
    }
}
