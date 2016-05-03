package com.nemator.needle.fragments.haystack;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.broadcastReceiver.LocationServiceBroadcastReceiver;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.GoogleMapDrawingUtils;
import com.nemator.needle.utils.MarkerUtils;
import com.nemator.needle.utils.PermissionManager;
import com.nemator.needle.views.UserMarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackMapFragment extends SupportMapFragment implements LocationServiceBroadcastReceiver.LocationServiceDelegate {

    public static final String TAG = "HaystackMapFragment";

    //Locations
    private ArrayList<UserVO> usersList = new ArrayList<UserVO>();
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;

    //Map
    private GoogleMap mMap;
    private UserMarker userMarker;
    private Circle haystackBoundsCircle;
    private Polygon haystackBoundsPolygon;

    //Map data
    private HashMap<Integer, UserMarker> mMarkers;
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
        if (PermissionManager.getInstance(getContext()).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Needle.serviceController.initServiceAndStartUpdates(getActivity());
        }else{
            PermissionManager.getInstance(getContext()).requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
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
            case R.id.menu_option_cancel_haystack:
                cancelHaystack();
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
        //Show Bounds
        if(haystack != null){
            if(haystack.getIsCircle()){
                if(haystackBoundsCircle == null){
                    CircleOptions circleOptions = new CircleOptions()
                            .center(haystack.getPositionLatLng())
                            .radius(haystack.getZoneRadius())
                            .fillColor(ContextCompat.getColor(getContext(), R.color.circleColor))
                            .strokeColor(ContextCompat.getColor(getContext(), R.color.primary_dark))
                            .strokeWidth(8);
                    haystackBoundsCircle = mMap.addCircle(circleOptions);
                }else{
                    haystackBoundsCircle.setCenter(haystack.getPositionLatLng());
                }
            }else{
                if(haystackBoundsPolygon == null){
                    haystackBoundsPolygon = GoogleMapDrawingUtils.drawHaystackPolygon(getContext(), mMap, haystack);
                }else{
                    haystackBoundsPolygon =  GoogleMapDrawingUtils.updateHaystackPolygon(haystackBoundsPolygon, haystack);
                }
            }
        }

        //Update user's marker
        if(mCurrentLocation != null){
            if(userMarker == null){
                userMarker = MarkerUtils.createUserMarker(getContext(), mMap, Needle.userModel.getUser(), mCurrentPosition, "Your Position");
            }else{
                userMarker.updateLocation(mMap, mCurrentPosition);
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
                    UserMarker marker;
                    LatLng position = new LatLng(lat, lng);

                    if(mMarkers == null){
                        mMarkers = new HashMap<Integer, UserMarker>();
                    }

                    if(mMarkers.containsKey(id)){
                        marker = mMarkers.get(id);
                        if(marker.getLocation() != null && !marker.getLocation().equals(position)){
                            marker.updateLocation(mMap, position);

                            Location loc = new Location("");
                            loc.setLatitude(lat);
                            loc.setLongitude(lng);

                            double distanceInMeters = Math.floor(mCurrentLocation.distanceTo(loc));

                            marker.setSnippet("Distance to " + id + " :" + distanceInMeters + "m");
                        }

                        //marker.showInfoWindow();
                    }else{
                        marker = MarkerUtils.createUserMarker(getContext(), mMap, user, position, user.getReadableUserName() + "'s Position");

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
                UserMarker marker = (UserMarker) mMarkers.get(markerId);

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

        //Show Bounds
        if(haystack != null){
            if(haystack.getIsCircle()){
                if(haystackBoundsCircle == null){
                    CircleOptions circleOptions = new CircleOptions()
                            .center(haystack.getPositionLatLng())
                            .radius(haystack.getZoneRadius())
                            .fillColor(ContextCompat.getColor(getContext(), R.color.circleColor))
                            .strokeColor(ContextCompat.getColor(getContext(), R.color.primary_dark))
                            .strokeWidth(8);
                    haystackBoundsCircle = mMap.addCircle(circleOptions);
                }else{
                    haystackBoundsCircle.setCenter(haystack.getPositionLatLng());
                }
            }else{
                if(haystackBoundsPolygon == null){
                    haystackBoundsPolygon = GoogleMapDrawingUtils.drawHaystackPolygon(getContext(), mMap, haystack);
                }else{
                    haystackBoundsPolygon =  GoogleMapDrawingUtils.updateHaystackPolygon(haystackBoundsPolygon, haystack);
                }
            }
        }

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        if(userMarker == null){
            userMarker = MarkerUtils.createUserMarker(getContext(), mMap, Needle.userModel.getUser(), mCurrentPosition, "Your Position");
        }else{
            userMarker.updateLocation(mMap, mCurrentPosition);
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
        ApiClient.getInstance().retrieveHaystackUserLocations(haystack.getId(), new Callback<UsersResult>() {
            @Override
            public void onResponse(Call<UsersResult> call, Response<UsersResult> response) {
                UsersResult result = response.body();

                if (result.getSuccessCode() == 1) {
                    //Log.d(TAG, "Retrieving locations success !");

                    usersList = result.getUsers();
                    updateMap();
                }else{
                    Log.d(TAG, "Retrieving locations failed : " + result.getMessage());

                }
            }

            @Override
            public void onFailure(Call<UsersResult> call, Throwable t) {
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

                if (!isActivated) {
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
        ApiClient.getInstance().leaveHaystack(Needle.userModel.getUser(), haystack, haystackLeftCallback);
    }

    private Callback<TaskResult> haystackLeftCallback = new Callback<TaskResult>(){

        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();

            if(result.getSuccessCode() == 1){
                Toast.makeText(getActivity(), "Haystack Left", Toast.LENGTH_SHORT).show();

                getActivity().finish();
            }else if (result.getSuccessCode() == 404){
                Toast.makeText(getActivity(), "Haystack Left", Toast.LENGTH_SHORT).show();

                getActivity().finish();

                Log.d(TAG, "Should not have been in that haystack, wtf !?");
            }else{
                Log.d(TAG, "Should have left haystack, wtf !? Success code : " + result.getSuccessCode());
                Toast.makeText(getActivity(), "Error Leaving Haystack", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {
            Toast.makeText(getActivity(), "Error Leaving Haystack", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error : " + t.getMessage());
        }
    };

    private void cancelHaystack(){
        new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getContext().getString(R.string.cancel))
                .setMessage(getContext().getString(R.string.cancel_haystack_confirmation))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ApiClient.getInstance().cancelHaystack(Needle.userModel.getUser(), haystack, haystackCancelledCallback);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //TODO : own class
    private Callback<TaskResult> haystackCancelledCallback = new Callback<TaskResult>(){
        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Haystack sucessfully cancelled !");//TODO : use constants
                Toast.makeText(getContext(), "Haystack sucessfully cancelled", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }else{
                Toast.makeText(getContext(), "Haystack cancellation failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {
            Toast.makeText(getContext(), "Haystack cancellation failed", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Haystack cancellation failed. Error : " + t.getMessage() );//TODO : use constants
        }
    };

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

    //Getters/Setters
    public Boolean isPostingLocationUpdates() {
        return mPostingLocationUpdates;
    }

    public LatLng getMapTarget() {
        return mMap.getCameraPosition().target;
    }
}
