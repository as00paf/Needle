package com.needletest.pafoid.needletest.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.needletest.pafoid.needletest.asynctask.PostLocation;
import com.needletest.pafoid.needletest.models.PostLocationParams;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOCATION_URL = AppConstants.PROJECT_URL + "locations.php";

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "isRequestingLocationUpdates";
    private static final String LOCATION_KEY = "currentLocation";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "lastUpdatedTime";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_LOCATION_ID = "id";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";

    private JSONArray mLocations = null;
    private ArrayList<HashMap<String, Object>> mLocationList;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = true;

    private GoogleMap mMap;
    private Marker mMarker;
    private HashMap<String, Marker> mMarkers;
    private String username = "";

    //Activity Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        updateValuesFromBundle(savedInstanceState);

        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            createLocationRequest();
            mGoogleApiClient.connect();
        }

        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            updateMap();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        new LoadLocations().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    //Location
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        updateMap();
        moveCamera();

        postLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
        updateMap();

        postLocation();
        new LoadLocations().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :

                        break;
                }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(this, "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    //Location Tasks
    public class LoadLocations extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            updateMap();
        }
    }

    public void updateJSONdata() {
        mLocationList = new ArrayList<HashMap<String, Object>>();
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(LOCATION_URL);

        try {
            int success = json.getInt(TAG_SUCCESS);
            String msg = json.getString(TAG_MESSAGE);
            Log.i("updateJSONdata","Success : "+success+", msg : "+msg+" "+json.toString());

            mLocations = json.getJSONArray(TAG_LOCATIONS);

            // looping through all locations according to the json object returned
            for (int i = 0; i < mLocations.length(); i++) {
                JSONObject c = mLocations.getJSONObject(i);

                String id = c.getString(TAG_LOCATION_ID);
                Double lat = c.getDouble(TAG_LAT);
                Double lng = c.getDouble(TAG_LNG);

                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put(TAG_LOCATION_ID, id);
                map.put(TAG_LAT, lat);
                map.put(TAG_LNG, lng);

                mLocationList.add(map);
            }

        } catch (JSONException e) {
            Log.i("updateJSONdata","error");
            e.printStackTrace();
        }
    }

    public void updateMap() {
        if(mCurrentLocation != null){
            // Report to the UI that the location was updated
            String msg = "Updated Location: " +
                    Double.toString(mCurrentLocation.getLatitude()) + "," +
                    Double.toString(mCurrentLocation.getLongitude());

            //Update user's marker
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();
            LatLng position = new LatLng(lat, lng);

            if(mMarker == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(position);
                mMarker = mMap.addMarker(markerOptions);
            }else{
                mMarker.setPosition(position);
            }

            mMarker.setTitle("Your Position");
            mMarker.setPosition(position);
            Log.i("updateMap() ", msg);
        }

        Log.i("updateMap","MARKERS TO ADD : "+mLocationList.size());
        for (int i = 0; i < mLocationList.size(); i++) {
            HashMap<String, Object> map = mLocationList.get(i);
            String id = map.get(TAG_LOCATION_ID).toString();
            Double lat = (Double) map.get(TAG_LAT);
            Double lng = (Double) map.get(TAG_LNG);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
            String username = sp.getString("username", "");

            Log.i("updateMap","ID : "+id);

            if(!TextUtils.isEmpty(id) && !id.equals(username)){
                Marker marker;
                LatLng position = new LatLng(lat, lng);

                if(mMarkers == null){
                    mMarkers = new HashMap<String, Marker>();
                }

                if(mMarkers.containsKey(id)){
                    marker = mMarkers.get(id);
                    if(marker.getPosition() != position){
                        marker.setPosition(position);
                        Log.i("updateMap","MOVING MARKER : "+id);

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

                    if(marker != null){
                        marker = mMap.addMarker(markerOptions);
                    }else{
                        marker.setPosition(position);
                    }

                    Log.i("updateMap","ADDING MARKER TO MAP : "+id);

                    marker.setTitle(id+"'s Position");
                    //marker.showInfoWindow();

                    mMarkers.put(id, marker);
                }
            }
        }
    }

    public void moveCamera(){
        Double lat = mCurrentLocation.getLatitude();
        Double lng = mCurrentLocation.getLongitude();
        LatLng position = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 100));
    }

    public void postLocation(){
        new PostLocation().execute(new PostLocationParams(this, getUserName(), mCurrentLocation));
    }

    private String getUserName(){
        if(username == ""){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(this);
            username = sp.getString("username", "");
        }

        return username;
    }
}
