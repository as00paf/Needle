package com.needletest.pafoid.needletest.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.needletest.pafoid.needletest.utils.ErrorDialogFragment;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // JSON IDS:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_LOCATION_ID = "id";
    private static final String TAG_USERNAME = "username";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";


    // An array of all of our locations
    private JSONArray mLocations = null;
    // manages all of our comments in a list.
    private ArrayList<HashMap<String, Object>> mLocationList;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker mMarker;
    HashMap<String, Marker> mMarkers;
    LocationClient mLocationClient;
    Location mCurrentLocation;
    LocationRequest mLocationRequest;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    JSONParser jsonParser = new JSONParser();

    // Progress Dialog
    private ProgressDialog pDialog;

    private static final String LOCATION_URL = AppConstants.PROJECT_URL + "locations.php";
    private static final String POST_LOCATION_URL = AppConstants.PROJECT_URL + "updateLocation.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        new LoadLocations().execute();
    }

    @Override
    protected void onStop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }

        mLocationClient.disconnect();
        super.onStop();
    }

    //Google Play Services APK detection
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }

        return false;
    }

    /*
    * Called by Location Services when the request to connect the
    * client finishes successfully. At this point, you can
    * request the current location or start periodic updates
    */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


        mCurrentLocation = mLocationClient.getLastLocation();

        MarkerOptions markerOptions = new MarkerOptions();
        Double lat = mCurrentLocation.getLatitude();
        Double lng = mCurrentLocation.getLongitude();
        LatLng position = new LatLng(lat, lng);
        markerOptions.position(position);



        if(mMarker == null){
            mMarker = mMap.addMarker(markerOptions);
        }else{
            mMarker.setPosition(position);
        }

        mMarker.setTitle("Your Position");

        Log.i("Latitude : ", Double.toString(lat));
        Log.i("Longitude : ", Double.toString(lng));
        
        //Move Camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 100));

        // If already requested, start periodic updates
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

        // Update location on server
        new PostLocation().execute(mCurrentLocation);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(this, "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        mCurrentLocation = location;
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        mMarker.setPosition(position);

        //Move Camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 100));

        Log.i("onLocationChanged", msg);
        new PostLocation().execute(mCurrentLocation);
        new LoadLocations().execute();
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mPrefs = getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
    }

    //Location Tasks
    public class LoadLocations extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Loading Locations...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            updateJSONdata();
            return null;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //pDialog.dismiss();
            updateMap();
        }
    }

    public void updateJSONdata() {

        // Instantiate the arraylist to contain all the JSON data.
        // we are going to use a bunch of key-value pairs, referring
        // to the json element name, and the content, for example,
        // message it the tag, and "I'm awesome" as the content..

        mLocationList = new ArrayList<HashMap<String, Object>>();

        // Bro, it's time to power up the J parser
        JSONParser jParser = new JSONParser();
        // Feed the beast our comments url, and it spits us
        // back a JSON object. Boo-yeah Jerome.
        JSONObject json = jParser.getJSONFromUrl(LOCATION_URL);

        // when parsing JSON stuff, we should probably
        // try to catch any exceptions:
        try {
            int success = json.getInt(TAG_SUCCESS);
            String msg = json.getString(TAG_MESSAGE);
            Log.i("updateJSONdata","Success : "+success+", msg : "+msg+" "+json.toString());

            // mLocations will tell us how many "locations" are
            // available
            mLocations = json.getJSONArray(TAG_LOCATIONS);

            // looping through all locations according to the json object returned
            for (int i = 0; i < mLocations.length(); i++) {
                JSONObject c = mLocations.getJSONObject(i);

                // gets the content of each tag
                String id = c.getString(TAG_LOCATION_ID);
                Double lat = c.getDouble(TAG_LAT);
                Double lng = c.getDouble(TAG_LNG);

                // creating new HashMap
                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put(TAG_LOCATION_ID, id);
                map.put(TAG_LAT, lat);
                map.put(TAG_LNG, lng);

                // adding HashList to ArrayList
                mLocationList.add(map);

                // annndddd, our JSON data is up to date same with our array
                // list
            }

        } catch (JSONException e) {
            Log.i("updateJSONdata","error");
            e.printStackTrace();
        }
    }

    public void updateMap() {
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

    class PostLocation extends AsyncTask<Location, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Posting Location ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected String doInBackground(Location... location) {
            // Check for success tag
            int success;
            String lat = Double.toString(location[0].getLatitude());
            String lng = Double.toString(location[0].getLongitude());

            //We need to change this:
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
            String username = sp.getString("username", "anon");

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("lat", lat));
                params.add(new BasicNameValuePair("lng", lng));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        POST_LOCATION_URL, "POST", params);

                // full json response
                Log.d("Post Location attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Location Added!", json.toString());
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Location Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
           /* pDialog.dismiss();
            if (file_url != null){
                Toast.makeText(MapsActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
            */
        }

    }
}
