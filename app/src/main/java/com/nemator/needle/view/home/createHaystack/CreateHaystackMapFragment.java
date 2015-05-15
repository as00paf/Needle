package com.nemator.needle.view.home.createHaystack;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.R;
import com.nemator.needle.tasks.GetAutoCompleteResultsTask.GetAutoCompleteResultsParams;
import com.nemator.needle.tasks.GetAutoCompleteResultsTask.GetAutoCompleteResultsTask;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                if (autoCompleteTask != null) {
                    autoCompleteTask.cancel(true);
                }
            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results
                if (autoCompleteTask != null) {
                    autoCompleteTask.cancel(true);
                }

                searchBox.clearSearchable();

                GetAutoCompleteResultsParams params =
                        new GetAutoCompleteResultsParams(getActivity(), searchBox, mGoogleApiClient, getPosition(), getZoneRadius(), searchBox.getSearchText());
                autoCompleteTask = new GetAutoCompleteResultsTask(params);
                autoCompleteTask.execute();
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
                if (autoCompleteTask != null) {
                    autoCompleteTask.cancel(true);
                }
            }
        });

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                searchBox.clearSearchable();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    searchBox.addSearchable(new SearchResult(placeLikelihood.getPlace().getName().toString(), getResources().getDrawable(R.drawable.ic_action_place)));

                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }

                likelyPlaces.release();
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

    //CLASSES
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
