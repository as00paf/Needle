package com.nemator.needle.fragments.haystack;

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
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.UsersTaskResult;
import com.nemator.needle.broadcastReceiver.LocationServiceBroadcastReceiver;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.leaveHaystack.LeaveHaystackParams;
import com.nemator.needle.tasks.leaveHaystack.LeaveHaystackTask;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackMapFragment extends SupportMapFragment
        implements LeaveHaystackTask.LeaveHaystackResponseHandler, LocationServiceBroadcastReceiver.LocationServiceDelegate {

    public static final String TAG = "HaystackMapFragment";

    //Locations
    private ArrayList<UserVO> usersList = new ArrayList<UserVO>();
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;

    //Map
    private GoogleMap mMap;
    private Marker mMarker;
    private Circle mCircle;

    //Map data
    private HashMap<Integer, Marker> mMarkers;
    private HaystackVO haystack;
    private Boolean cameraUpdated = false;

    //Location Service
    private LocationServiceBroadcastReceiver locationServiceBroadcastReceiver;
    private Boolean mPostingLocationUpdates = false;
    private Boolean mRequestingLocationUpdates = true;
    private Boolean isActivated = false;

    //Constructors
    public static HaystackMapFragment newInstance() {
        HaystackMapFragment fragment = new HaystackMapFragment();
        return fragment;
    }

    public HaystackMapFragment(){

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
        Needle.serviceController.initServiceAndStartUpdates(getActivity());

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
        }

        haystack = ((HaystackActivity) getActivity()).getHaystack();
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
        haystack = ((HaystackActivity) getActivity()).getHaystack();
        getActivity().registerReceiver(locationServiceBroadcastReceiver, new IntentFilter(AppConstants.LOCATION_UPDATED));

        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(isActivated == false)
            Needle.serviceController.getService().stopLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(locationServiceBroadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        //stopLocationUpdates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.location_sharing:
                toggleLocationSharing();
                item.setIcon(isPostingLocationUpdates() ?
                        getResources().getDrawable(R.drawable.ic_my_location_white_24dp) :
                        getResources().getDrawable(R.drawable.ic_location_disabled_white_24dp));
                return true;
            case R.id.menu_option_leave:
                leaveHaystack();
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

        retrieveLocations();
    }

    //Map methods
    public void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }else if(Needle.serviceController.isConnected()){
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

        if(usersList != null && usersList.size() > 0){
            Iterator<UserVO> iterator = usersList.iterator();
            while(iterator.hasNext()){
                UserVO user = iterator.next();

                int id =  user.getId();
                Double lat = user.getLocation().getLatitude();
                Double lng = user.getLocation().getLongitude();

                if(id != Needle.userModel.getUserId()){
                    Marker marker;
                    LatLng position = new LatLng(lat, lng);

                    if(mMarkers == null){
                        mMarkers = new HashMap<Integer, Marker>();
                    }

                    if(mMarkers.containsKey(id)){
                        marker = mMarkers.get(id);
                        if(!marker.getPosition().equals(position)){
                            animateMarker(mMap, marker, position, false);

                            Location loc = new Location("");
                            loc.setLatitude(lat);
                            loc.setLongitude(lng);

                            double distanceInMeters = Math.floor(mCurrentLocation.distanceTo(loc));

                            marker.setSnippet("Distance to " + id + " :" + distanceInMeters + "m");
                        }

                        //marker.showInfoWindow();
                    }else{
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(position);

                        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                        markerOptions.icon(icon);

                        marker = mMap.addMarker(markerOptions);
                        marker.setPosition(position);

                        Log.i(TAG,"Adding marker with id : "+id+" to map.");

                        String name = user.getUserName();
                        marker.setTitle(name+"'s Position");
                        marker.showInfoWindow();

                        mMarkers.put(id, marker);
                    }
                }
            }
        }

        //Remove no longer active Markers
        if(mMarkers != null){
            Iterator markerIterator = mMarkers.keySet().iterator();
            while(markerIterator.hasNext()) {
                int markerId = (int) markerIterator.next();
                Marker marker = (Marker) mMarkers.get(markerId);

                Boolean isActive = false;

                if(usersList != null){
                    Iterator<UserVO> iterator = usersList.iterator();
                    while(iterator.hasNext()){
                        UserVO user = iterator.next();

                        if(user.getId() == markerId){
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
    }

    private void resumeOperations(){
        if(mMap==null)
            mMap = getMap();

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        if(mMarker == null && mCircle == null){
            markerOptions.position(mCurrentPosition);
            drawMarkerWithCircle(mCurrentPosition, "Your Position");
        }else{
            updateMarkerWithCircle(mCurrentPosition);
        }

        //Add other user's markers back
        if(usersList!=null){
            Log.i(TAG,"Markers to add : "+usersList.size());
            Iterator<UserVO> iterator = usersList.iterator();
            while(iterator.hasNext()){
                UserVO user = iterator.next();

                int id = user.getId();
                Double lat = user.getLocation().getLatitude();
                Double lng = user.getLocation().getLongitude();

                if(id != Needle.userModel.getUserId()){
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

        updateMap();
        moveCamera();

        retrieveLocations();
    }

    public void moveCamera(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 19.0f));
        cameraUpdated = true;
    }

    //Actions
    public void retrieveLocations(){
        ApiClient.getInstance().retrieveHaystackUserLocations(haystack.getId(), new Callback<UsersTaskResult>() {
            @Override
            public void onResponse(Call<UsersTaskResult> call, Response<UsersTaskResult> response) {
                UsersTaskResult result = response.body();

                if (result.getSuccessCode() == 1) {
                    //Log.d(TAG, "Retrieving locations success !");

                    usersList = result.getUsers();
                    updateMap();
                }else{
                    Log.d(TAG, "Retrieving locations failed : " + result.getMessage());

                }
            }

            @Override
            public void onFailure(Call<UsersTaskResult> call, Throwable t) {
                Log.d(TAG, "Retrieving locations failed ! Error : " + t.getMessage());
                usersList = null;
            }
        });
    }

    public void activateUser(){
        Log.d(TAG, "Activating user");

        ApiClient.getInstance().activateUser(Needle.userModel.getUser(), haystack, new Callback<TaskResult>() {
            @Override
            public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
                TaskResult result = response.body();
                isActivated = result.getSuccessCode() == 1;

                MenuItem item = ((HaystackActivity) getActivity()).getMenu().findItem(R.id.location_sharing);
                item.setIcon(isActivated ?
                        getResources().getDrawable(R.drawable.ic_location_disabled_white_24dp) :
                        getResources().getDrawable(R.drawable.ic_my_location_white_24dp));

                if(!isActivated){
                    Toast.makeText(getActivity(), "Error Sharing Location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TaskResult> call, Throwable t) {
                MenuItem item = ((HaystackActivity) getActivity()).getMenu().findItem(R.id.location_sharing);
                item.setIcon(isActivated ?
                        getResources().getDrawable(R.drawable.ic_location_disabled_white_24dp) :
                        getResources().getDrawable(R.drawable.ic_my_location_white_24dp));

                Toast.makeText(getActivity(), "Error Sharing Location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deactivateUser(){
        Log.d(TAG, "Deactivating user");

        Needle.userModel.getUser().setIsActive(false);

        ApiClient.getInstance().deactivateUser(Needle.userModel.getUser(), haystack, new Callback<TaskResult>() {
            @Override
            public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
                TaskResult result = response.body();
                isActivated = !(result.getSuccessCode() == 1);

                MenuItem item = ((HaystackActivity) getActivity()).getMenu().findItem(R.id.location_sharing);
                item.setIcon(!isActivated ?
                        getResources().getDrawable(R.drawable.ic_my_location_white_24dp) :
                        getResources().getDrawable(R.drawable.ic_location_disabled_white_24dp));

                if (response.body().getSuccessCode() == 0) {
                    Toast.makeText(getActivity(), "Error Deactivating User", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TaskResult> call, Throwable t) {
                MenuItem item = ((HaystackActivity) getActivity()).getMenu().findItem(R.id.location_sharing);
                item.setIcon(isActivated ?
                        getResources().getDrawable(R.drawable.ic_my_location_white_24dp) :
                        getResources().getDrawable(R.drawable.ic_location_disabled_white_24dp));

                Toast.makeText(getActivity(), "Error Deactivating User", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveHaystack(){
        LeaveHaystackParams params = new LeaveHaystackParams(getActivity(), String.valueOf(Needle.userModel.getUserId()), String.valueOf(haystack.getId()));
        try{
            LeaveHaystackTask task = new LeaveHaystackTask(params, this);
            task.execute();
        }catch(Exception e){
            Toast.makeText(getActivity(), "Error Leaving Haystack", Toast.LENGTH_SHORT).show();
        }
    }

    public void onHaystackLeft(TaskResult result){
        if(result.getSuccessCode() == 1){
            Toast.makeText(getActivity(), "Haystack Left", Toast.LENGTH_SHORT).show();

           /* Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);*/
        }else{
            Toast.makeText(getActivity(), "Error Leaving Haystack", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareLocation(){
        mPostingLocationUpdates = true;
        activateUser();
        Needle.serviceController.getService()
                .addPostLocationRequest(LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_HAYSTACK, haystack.getTimeLimit(), haystack.getOwner(), String.valueOf(haystack.getId()));

        Needle.serviceController.getService()
                .postLocation();
    }

    public void stopSharingLocation(){
        mPostingLocationUpdates = false;
        Needle.serviceController.getService()
                .removePostLocationRequest(LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_HAYSTACK, haystack.getTimeLimit(), haystack.getOwner(), String.valueOf(haystack.getId()));
        deactivateUser();
    }

    public void toggleLocationSharing(){
        if(mPostingLocationUpdates){
            stopSharingLocation();
        }else{
            shareLocation();
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
    public Boolean isPostingLocationUpdates() {
        return mPostingLocationUpdates;
    }
}
