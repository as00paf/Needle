package com.needletest.pafoid.needletest.haystack;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.task.activate.ActivateUserParams;
import com.needletest.pafoid.needletest.haystack.task.activate.ActivateUserTask;
import com.needletest.pafoid.needletest.haystack.task.deactivate.DeactivateUserParams;
import com.needletest.pafoid.needletest.haystack.task.deactivate.DeactivateUserTask;
import com.needletest.pafoid.needletest.haystack.task.postLocation.PostLocationParams;
import com.needletest.pafoid.needletest.haystack.task.postLocation.PostLocationTask;
import com.needletest.pafoid.needletest.haystack.task.retrieveLocations.RetrieveLocationsParams;
import com.needletest.pafoid.needletest.haystack.task.retrieveLocations.RetrieveLocationsResult;
import com.needletest.pafoid.needletest.haystack.task.retrieveLocations.RetrieveLocationsTask;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.TaskResult;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class CustomSupportMapFragment extends SupportMapFragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = "CustomSupportMapFragment";

    private ArrayList<HashMap<String, Object>> mLocationList = new ArrayList<HashMap<String, Object>>();

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = true;
    private Boolean mPostingLocationUpdates = false;
    private Boolean isActivated = false;
    private Boolean locationUpdatesStarted = false;

    private GoogleMap mMap;
    private Marker mMarker;
    private HashMap<String, Marker> mMarkers;
    private String username = "";
    private int userId = -1;
    private Haystack haystack;
    private String haystackId;
    private Circle mCircle;
    private Boolean cameraUpdated = false;

    public static CustomSupportMapFragment newInstance(Haystack haystack) {
        CustomSupportMapFragment fragment = new CustomSupportMapFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        fragment.setArguments(args);

        fragment.haystack = haystack;
        return fragment;
    }

    public CustomSupportMapFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            updateValuesFromBundle(savedInstanceState);
        }
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

            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }
        }else{
            haystack = (Haystack) getActivity().getIntent().getExtras().get(AppConstants.HAYSTACK_DATA_KEY);
        }

        if(haystack != null) haystackId = String.valueOf(haystack.getId());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(AppConstants.LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(AppConstants.LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        //super.onSaveInstanceState(savedInstanceState);
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

        if(isActivated) deactivateUser();
    }


    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    //Location Methods
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

    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            createLocationRequest();
            mGoogleApiClient.connect();
        }
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

        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null){
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();
            mCurrentPosition = new LatLng(lat, lng);

            updateMap();
            moveCamera();

            postLocation();
        }

        retrieveLocations();
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

        postLocation();
        retrieveLocations();
    }

    private void resumeOperations(){
        if(mMap==null)
            mMap = getMap();

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCurrentPosition);
        mMarker = mMap.addMarker(markerOptions);

        //Add other user's markers back
        if(mLocationList!=null){
            Log.i(TAG,"Markers to add : "+mLocationList.size());
            for (int i = 0; i < mLocationList.size(); i++) {
                HashMap<String, Object> map = mLocationList.get(i);
                String id = map.get(AppConstants.TAG_USER_ID).toString();
                Double lat = (Double) map.get(AppConstants.TAG_LAT);
                Double lng = (Double) map.get(AppConstants.TAG_LNG);

                if(!TextUtils.isEmpty(id) && !id.equals(getUserName())){
                    Marker marker;
                    LatLng position = new LatLng(lat, lng);

                    markerOptions = new MarkerOptions();
                    markerOptions.position(position);
                    BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                    markerOptions.icon(icon);

                    marker = mMap.addMarker(markerOptions);
                    marker.setPosition(position);

                    Log.i(TAG,"Adding marker with id : "+id+" to map.");

                    marker.setTitle(id+"'s Position");
                    marker.showInfoWindow();
                }
            }
        }

        if(!locationUpdatesStarted)
            startLocationUpdates();

        updateMap();
        moveCamera();

        postLocation();
        retrieveLocations();
    }

    public void updateMap() {
        //Update user's marker
        if(mCurrentLocation != null){
            if(mMarker == null && mCircle == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurrentPosition);
                drawMarkerWithCircle(mCurrentPosition);
            }else{
                updateMarkerWithCircle(mCurrentPosition);
            }

            mMarker.setTitle("Your Position");
        }

        //Log.i(TAG,"Markers to add : "+mLocationList.size());
        for (int i = 0; i < mLocationList.size(); i++) {
            HashMap<String, Object> map = mLocationList.get(i);
            String id = map.get(AppConstants.TAG_USER_ID).toString();
            Double lat = (Double) map.get(AppConstants.TAG_LAT);
            Double lng = (Double) map.get(AppConstants.TAG_LNG);

            if(!TextUtils.isEmpty(id) && !id.equals(getUserName())){
                Marker marker;
                LatLng position = new LatLng(lat, lng);

                if(mMarkers == null){
                    mMarkers = new HashMap<String, Marker>();
                }

                if(mMarkers.containsKey(id)){
                    marker = mMarkers.get(id);
                    if(!marker.getPosition().equals(position)){
                        //marker.setPosition(position);
                        animateMarker(marker, position, false);


                        //Log.i(TAG,"Moving marker with id : "+id);

                        /*Location loc = new Location("");
                        loc.setLatitude(lat);
                        loc.setLongitude(lng);

                        double distanceInMeters = Math.floor(mCurrentLocation.distanceTo(loc));

                        marker.setSnippet("Distance to " + id + " :" + distanceInMeters + "m");*/
                    }

                    //marker.showInfoWindow();
                }else if(!(id.equals(String.valueOf(getUserId())))){
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(position);

                    BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                    markerOptions.icon(icon);

                    marker = mMap.addMarker(markerOptions);
                    marker.setPosition(position);

                    //Log.i(TAG,"Adding marker with id : "+id+" to map.");

                    String name = map.get(AppConstants.TAG_USER_NAME).toString();
                    marker.setTitle(name+"'s Position");
                    marker.showInfoWindow();

                    mMarkers.put(id, marker);
                }
            }
        }

        //Remove no longer active Markers
        if(mMarkers != null){
            Iterator markerIterator = mMarkers.keySet().iterator();
            while(markerIterator.hasNext()) {
                String markerId =(String) markerIterator.next();
                Marker marker = (Marker) mMarkers.get(markerId);

                Boolean isActive = false;

                for (int i = 0; i < mLocationList.size(); i++) {
                    HashMap<String, Object> map = mLocationList.get(i);
                    String id = map.get(AppConstants.TAG_USER_ID).toString();

                    if(id.equals(markerId)){
                        isActive = true;
                    }
                }

                if(!isActive){
                    marker.remove();
                    mMarkers.remove(marker);
                }
            }
        }
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
        double radiusInMeters = 10.0;
        int strokeColor = getResources().getColor(R.color.primary);
        int shadeColor = getResources().getColor(R.color.circleColor);

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = mMap.addMarker(markerOptions);
    }

    private void updateMarkerWithCircle(LatLng position) {
        if(!mCircle.getCenter().equals(position)){
            mCircle.setCenter(position);
        }

        if(!mMarker.getPosition().equals(position)){
            animateMarker(mMarker, position, false);
        }
    }

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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(getActivity(), "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    public void moveCamera(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 100));
        cameraUpdated = true;
    }

    public void postLocation(){
        if(!mPostingLocationUpdates){
            return;
        }

        PostLocationParams params = new PostLocationParams(getActivity(), getUserName(), String.valueOf(getUserId()), mCurrentLocation, mCurrentPosition, false);
        new PostLocationTask(params).execute();
    }

    public void retrieveLocations(){
        RetrieveLocationsParams params = new RetrieveLocationsParams(getUserName(), String.valueOf(getUserId()), haystackId, false);
        try{
            RetrieveLocationsResult result = new RetrieveLocationsTask(params).execute().get();
            mLocationList = result.locationList;
        }catch (Exception e){
            mLocationList = null;
        }
    }

    public void activateUser(){
        ActivateUserParams params = new ActivateUserParams(getActivity(), String.valueOf(getUserId()), String.valueOf(haystack.getId()));
        isActivated = false;

        try{
            TaskResult result = new ActivateUserTask(params).execute().get();
            isActivated = result.successCode == 1;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void deactivateUser(){
        DeactivateUserParams params = new DeactivateUserParams(getActivity(), String.valueOf(getUserId()), String.valueOf(haystack.getId()));

        try{
            TaskResult result = new DeactivateUserTask(params).execute().get();
            isActivated = !(result.successCode == 1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shareLocation(){
        mPostingLocationUpdates = true;
        activateUser();
        postLocation();
    }

    public void stopSharingLocation(){
        mPostingLocationUpdates = false;
        deactivateUser();
    }

    public void toggleLocationSharing(){
        if(mPostingLocationUpdates){
            stopSharingLocation();
        }else{
            shareLocation();
        }
    }

    private String getUserName(){
        if(username == ""){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            username = sp.getString("username", "");
        }

        return username;
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    public Boolean isPostingLocationUpdates() {
        return mPostingLocationUpdates;
    }

}
