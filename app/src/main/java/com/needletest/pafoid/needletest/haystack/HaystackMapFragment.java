package com.needletest.pafoid.needletest.haystack;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.task.PostLocationParams;
import com.needletest.pafoid.needletest.haystack.task.PostLocationTask;
import com.needletest.pafoid.needletest.haystack.task.RetrieveLocationsParams;
import com.needletest.pafoid.needletest.haystack.task.RetrieveLocationsResult;
import com.needletest.pafoid.needletest.haystack.task.RetrieveLocationsTask;
import com.needletest.pafoid.needletest.models.Haystack;

import org.json.JSONArray;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HaystackMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{
    public static final String TAG = "HaystackMapFragment";

    private JSONArray mLocations = null;
    private ArrayList<HashMap<String, Object>> mLocationList = new ArrayList<HashMap<String, Object>>();

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = true;

    private GoogleMap mMap;
    private Marker mMarker;
    private HashMap<String, Marker> mMarkers;
    private String username = "";
    private int userId = -1;
    private Haystack haystack;
    private String haystackId;

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private SupportMapFragment mMapFragment;

    public static HaystackMapFragment newInstance() {
        HaystackMapFragment fragment = new HaystackMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HaystackMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        haystack = (Haystack) getActivity().getIntent().getExtras().get(AppConstants.HAYSTACK_DATA_KEY);
        haystackId = String.valueOf(haystack.getId());
        updateMap();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(AppConstants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(AppConstants.LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(AppConstants.LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_haystack_map, container, false);

        updateValuesFromBundle(savedInstanceState);

        //Map
        /*if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services Unavailable");
        }else{
            setUpMapIfNeeded();
        }*/


        mMapFragment = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                setUpMapIfNeeded();
            }
        };

        getChildFragmentManager().beginTransaction().add(R.id.haystack_map_container, mMapFragment).commit();

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

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

    //Location Methods
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = mMapFragment.getMap();

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
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Double lat = mCurrentLocation.getLatitude();
        Double lng = mCurrentLocation.getLongitude();
        mCurrentPosition = new LatLng(lat, lng);

        updateMap();
        moveCamera();

        postLocation();
        RetrieveLocationsParams params =  new RetrieveLocationsParams(getUserName(), String.valueOf(getUserId()), haystackId);
        new RetrieveLocationsTask(params).execute();
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

        updateMap();

        postLocation();
        retrieveLocations();
    }

    private void resumeOperations(){
        mMap = mMapFragment.getMap();

        //Add user's marker back
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mCurrentPosition);
        mMarker = mMap.addMarker(markerOptions);

        //Add other user's markers back
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

        startLocationUpdates();
        updateMap();
        moveCamera();

        postLocation();
        RetrieveLocationsParams params =  new RetrieveLocationsParams(getUserName(), String.valueOf(getUserId()), haystackId);
        new RetrieveLocationsTask(params).execute();
    }

    public void updateMap() {
        //Update user's marker
        if(mCurrentLocation != null){
            if(mMarker == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurrentPosition);
                mMarker = mMap.addMarker(markerOptions);
            }

            mMarker.setTitle("Your Position");
            mMarker.setPosition(mCurrentPosition);
        }

        Log.i(TAG,"Markers to add : "+mLocationList.size());
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
                    if(marker.getPosition() != position){
                        marker.setPosition(position);
                        Log.i(TAG,"Moving marker with id : "+id);

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

                    marker.setTitle(id+"'s Position");
                    marker.showInfoWindow();

                    mMarkers.put(id, marker);
                }
            }
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
    }

    public void postLocation(){
        PostLocationParams params = new PostLocationParams(getActivity(), getUserName(), String.valueOf(getUserId()), mCurrentLocation, mCurrentPosition);
        new PostLocationTask(params).execute();
    }

    public void retrieveLocations(){
        RetrieveLocationsParams params = new RetrieveLocationsParams(getUserName(), String.valueOf(getUserId()), haystackId);
        try{
            RetrieveLocationsResult result = new RetrieveLocationsTask(params).execute().get();
            mLocationList = result.locationList;
        }catch (Exception e){

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
