package com.nemator.needle.home.createHaystack;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.R;
import com.nemator.needle.haystack.task.postLocation.PostLocationParams;
import com.nemator.needle.haystack.task.postLocation.PostLocationResult;
import com.nemator.needle.utils.SphericalUtil;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateHaystackMapFragment extends CreateHaystackBaseFragment{
    public static String TAG = "CreateHaystackMapFragment";

    public View rootView;

    private GoogleMap mMap;

    Boolean mIsMapMoveable = false;

    public static boolean mMapIsTouched = false;
    Projection projection;
    public double latitude;
    public double longitude;
    ArrayList<LatLng> val = new ArrayList<LatLng>();
    private float mScaleFactor = 1.f;
    public CreateHaystackMap mMapFragment;
    private ScaleGestureDetector mScaleDetector;
    private Boolean mIsCircle = true;
    private SearchBox searchBox;
    private Boolean isPublic = false;

    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;
    private GetAutoCompleteResultsTask autoCompleteTask;

    public static CreateHaystackMapFragment newInstance() {
        CreateHaystackMapFragment fragment = new CreateHaystackMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        connectToApiClient();

        setHasOptionsMenu(true);
        this.setRetainInstance(true);
    }

    private void connectToApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = ((CreateHaystackFragment) getParentFragment()).getGoogleApiClient();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_map, container, false);

        //Map Fragment
        mMapFragment = (CreateHaystackMap) getChildFragmentManager().findFragmentById(R.id.create_haystack_map);

        mScaleDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());

        FrameLayout mapFrame = (FrameLayout) rootView.findViewById(R.id.create_haystack_map_frame);
        mapFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                if (mIsMapMoveable == true) {
                    return true;
                } else {
                    return false;
                }
            }
            });

        //Search Box
        searchBox = (SearchBox) rootView.findViewById(R.id.create_haystack_map_search_box);
        searchBox.enableVoiceRecognition(this);
        searchBox.setLogoText(getString(R.string.search_for_places));
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                if(autoCompleteTask != null){
                    autoCompleteTask.cancel(true);
                }
            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results
                if(autoCompleteTask != null){
                    autoCompleteTask.cancel(true);
                }

                searchBox.clearSearchable();

                autoCompleteTask = new GetAutoCompleteResultsTask();
                autoCompleteTask.execute(searchBox.getSearchText());
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(getActivity(), searchTerm + " Searched", Toast.LENGTH_LONG).show();
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(searchTerm, 2);
                    LatLng location = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    mMapFragment.moveCameraTo(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSearchCleared() {
                if(autoCompleteTask != null){
                    autoCompleteTask.cancel(true);
                }
            }
        });

        //Map Buttons
        ImageButton btn_draw_State = (ImageButton) rootView.findViewById(R.id.btn_draw_State);
        btn_draw_State.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsMapMoveable != true) {
                    mIsMapMoveable = true;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_edit_black_24dp));
                } else {
                    mIsMapMoveable = false;
                  //  ((Button) v).setText("Free Draw");
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_vpn_lock_black_24dp));
                }

            }
        });

        ImageButton btnPolygon = (ImageButton) rootView.findViewById(R.id.btn_polygon);
        btnPolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCircle != true) {
                    mIsCircle = true;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.square24));
                } else {
                    mIsCircle = false;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.circle24));
                }

                mMapFragment.setIsPolygonCircle(mIsCircle);
            }
        });

        return rootView;
    }

    public int getZoneRadius(){
        return mMapFragment.getZoneRadius();
    }

    public Boolean getIsCircle(){
        return mIsCircle;
    }

    public LatLng getPosition(){
        return  mMapFragment.getPosition();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public CreateHaystackMap getMap(){
        return mMapFragment;
    }

    class GetAutoCompleteResultsTask extends AsyncTask<String, Void, Void> {
        public GetAutoCompleteResultsTask(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... args) {
            String constraint = args[0];
            if (mGoogleApiClient.isConnected()) {
                Log.i(TAG, "Starting autocomplete query for: " + constraint);



                LatLng southWest = SphericalUtil.computeOffset(getPosition(), getZoneRadius(), 135);
                LatLng northEast = SphericalUtil.computeOffset(getPosition(), getZoneRadius(), 315);
                mBounds = new LatLngBounds(southWest, northEast);

                // Submit the query to the autocomplete API and retrieve a PendingResult that will
                // contain the results when the query completes.
                PendingResult<AutocompletePredictionBuffer> results =
                        Places.GeoDataApi
                                .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                        mBounds, mPlaceFilter);

                // This method should have been called off the main UI thread. Block and wait for at most 60s
                // for a result from the API.
                AutocompletePredictionBuffer autocompletePredictions = results
                        .await(60, TimeUnit.SECONDS);

                // Confirm that the query completed successfully, otherwise return null
                final com.google.android.gms.common.api.Status status = autocompletePredictions.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());

                    /*Toast.makeText(getActivity(), "Error contacting API: " + status.toString(),
                            Toast.LENGTH_SHORT).show();*/

                    autocompletePredictions.release();
                    return null;
                }

                Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");

                // Copy the results into our own data structure, because we can't hold onto the buffer.
                // AutocompletePrediction objects encapsulate the API response (place ID and description).

                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                    resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getDescription()));
                }

                // Release the buffer now that all data has been copied.
                autocompletePredictions.release();

                ArrayList<PlaceAutocomplete> autoCompleteResults = resultList;
                for (int i = 0; i < autoCompleteResults.size(); i++) {
                    String searchTerm = autoCompleteResults.get(i).description.toString();
                    SearchResult searchResult = new SearchResult(searchTerm, getResources().getDrawable(R.drawable.ic_action_place));
                    if(!searchBox.getSearchables().contains(searchResult)){
                        searchBox.addSearchable(searchResult);
                    }
                }

                return null;
            }
            Log.e(TAG, "Google API client is not connected for autocomplete query.");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //CLASSES
    /**
     * Holder for Places Geo Data Autocomplete API results.
     */
    class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            mMapFragment.setScaleFactor(mScaleFactor);
            return true;
        }
    }
}
