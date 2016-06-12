package com.nemator.needle.fragments.haystacks.createHaystack;

import android.Manifest;
import android.animation.Animator;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.CreateHaystackActivity;
import com.nemator.needle.controller.GoogleMapCameraController;
import com.nemator.needle.controller.GoogleMapCameraControllerConfig;
import com.nemator.needle.controller.GoogleMapController;
import com.nemator.needle.fragments.SearchFragment;
import com.nemator.needle.tasks.getAutoCompleteResults.GetAutoCompleteResultsTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.GoogleMapDrawingUtils;
import com.nemator.needle.utils.PermissionManager;
import com.nemator.needle.utils.SphericalUtil;

public class CreateHaystackMapFragment extends CreateHaystackBaseFragment implements GoogleMapController.GoogleMapCallback {
    public static String TAG = "CreateHaystackMapFragment";

    //Views
    private ImageButton lockMapButton, haystackShapeButton, myLocationButton;
    private TextView mRadiusLabel;
    private SupportMapFragment mapFragment;
    private Menu menu;

    //Data
    private Boolean mIsMapMoveable = false;
    private Circle mCircle;
    private Polygon mPolygon;
    private Boolean mIsPolygonCircle = true;
    private Float mScaleFactor = 1.0f;

    //Objects
    private ScaleGestureDetector mScaleDetector;
    private GetAutoCompleteResultsTask autoCompleteTask;

    //Search
    private SearchView searchView;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private SearchFragment searchFragment;
    private GoogleMapController mapController;

    public static CreateHaystackMapFragment newInstance() {
        CreateHaystackMapFragment fragment = new CreateHaystackMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackMapFragment() {
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received API connected Intent");

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.unregisterReceiver(this);

            addPlaceSuggestion();
            mapController.focusOnMyPosition();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_map, container, false);

        //Map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.create_haystack_map);
        mapController = new GoogleMapController(getActivity(), new GoogleMapCameraControllerConfig(), this);
        mapController.initMap(mapFragment);

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

        //Search
        searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.searchFragment);

        //Radius Indicator
        mRadiusLabel = (TextView) rootView.findViewById(R.id.radius_label);
        mRadiusLabel.setText(String.valueOf(getZoneRadius()) + "m");
        initBroadcastListeners();

        if (!PermissionManager.getInstance(getActivity()).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionManager.getInstance(getActivity()).requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //Buttons
        lockMapButton = (ImageButton) rootView.findViewById(R.id.lock_map_button);
        lockMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLockMap();
            }
        });

        haystackShapeButton = (ImageButton) rootView.findViewById(R.id.map_shape_button);
        haystackShapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHaystackShape();
            }
        });

        myLocationButton = (ImageButton) rootView.findViewById(R.id.my_position_button);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapController.cameraController.focusOnMyPosition();
            }
        });


        return rootView;
    }

    private void updateMap() {
        //Update user's marker and polygon
        LatLng position = mapController.getCurrentCameraTarget();
        if (position != null) {
            if (mIsPolygonCircle) {
                if (mCircle == null) {
                    mCircle = GoogleMapDrawingUtils.drawCircle(mapController.getGoogleMap(), position, mScaleFactor, getResources(), mCircle);
                } else {
                    mCircle = GoogleMapDrawingUtils.updateCircle(mCircle, position, mScaleFactor);
                }
            } else {
                if (mPolygon == null) {
                    mPolygon = GoogleMapDrawingUtils.drawPolygon(mapController.getGoogleMap(), position, mScaleFactor, getResources(), mPolygon);
                } else {
                    mPolygon = GoogleMapDrawingUtils.updatePolygon(mPolygon, position, mScaleFactor);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return false;
            }
        });

        mSearchAction = menu.findItem(R.id.action_search);

        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                handleMenuSearch();
                break;
            case android.R.id.home:
                if (isSearchOpened) {
                    handleMenuSearch();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleLockMap(){
        if (mIsMapMoveable != true) {
            mIsMapMoveable = true;
            lockMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_edit_location_black_24dp));
        } else {
            mIsMapMoveable = false;
            lockMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_vpn_lock_black_24dp));
        }

        updateMap();
    }

    private void toggleHaystackShape(){
        if (mIsPolygonCircle != true) {
            if (mPolygon != null) {
                mPolygon.remove();
                mPolygon = null;
            }

            mIsPolygonCircle = true;
            haystackShapeButton.setImageDrawable(getResources().getDrawable(R.drawable.square_black_24));
        } else {
            if (mCircle != null) {
                mCircle.remove();
                mCircle = null;
            }

            mIsPolygonCircle = false;
            haystackShapeButton.setImageDrawable(getResources().getDrawable(R.drawable.circle_black_24));
        }

        updateMap();
    }

    private void handleMenuSearch() {
        if (isSearchOpened) {
            closeSearchMenu();
        } else {
            isSearchOpened = true;
            searchFragment.show(null);
        }
    }

    private void initBroadcastListeners() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        localBroadcastManager.registerReceiver(searchStateListener,
                new IntentFilter(getString(R.string.action_search_started)));

        localBroadcastManager.registerReceiver(searchStateListener,
                new IntentFilter(getString(R.string.action_search_finished)));

        localBroadcastManager.registerReceiver(searchItemListener,
                new IntentFilter(getString(R.string.action_search_item_selected)));
    }

    private BroadcastReceiver searchStateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Boolean showProgressBar = action == getString(R.string.action_search_started);
            //TODO : add progress bar
            //progressBar.setVisibility(showProgressBar ? View.VISIBLE : View.INVISIBLE);
        }
    };

    private BroadcastReceiver searchItemListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLng location = intent.getParcelableExtra(getString(R.string.location));
            if (location != null) {
                closeSearchMenu();

                mapController.cameraController.zoom(location);
            }
        }
    };

    private void doSearch(String query) {
        if(!query.isEmpty()){
            Log.d(TAG, "doSearch::query : " + query);

            LatLngBounds bounds = SphericalUtil.toBounds(getCameraTarget(), 50000);//Guessing average city size at 50 km2
            searchFragment.guessLocation(query, bounds);
        }
    }

    public void closeSearchMenu() {
        mSearchAction.collapseActionView();

        searchFragment.hide();
        isSearchOpened = false;
        updateMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapController.setLocationUpdates(true);
            addPlaceSuggestion();
        }
    }

    private void addPlaceSuggestion() {
        if (Needle.googleApiController.getGoogleApiClient() != null) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return;
            }

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(Needle.googleApiController.getGoogleApiClient(), null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {


                /*searchBox.clearSearchable();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    searchBox.addSearchable(new SearchResult(placeLikelihood.getPlace().getName().toString(), getResources().getDrawable(R.drawable.ic_action_place)));

                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }
*/
                    likelyPlaces.release();
                }
            });
        }else{

        }
    }

    public int getZoneRadius(){
        return (int) Math.round(mScaleFactor * 50.0);
    }

    public Boolean getIsCircle() {
        return mIsPolygonCircle;
    }

    public LatLng getCameraTarget() {
        return mapController.getCurrentCameraTarget();
    }

    public LatLngBounds getCameraTargetBounds() {
        return mapController.getCurrentCameraTargetBounds();
    }

    @Override
    public void onMapInitialized(GoogleMap googleMap) {
        mapController.cameraController.setCameraChangedListener(new GoogleMapCameraController.CameraChangedListener() {
            @Override
            public void onCameraChanged(CameraPosition cameraPosition) {
                updateMap();
            }
        });
    }

    public GoogleMapController getMapController() {
        return mapController;
    }

    public LatLng getPosition() {
        return mapController.getCurrentCameraTarget();
    }

    public boolean isSearchOpened() {
        return isSearchOpened;
    }

    //CLASSES
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            mRadiusLabel.setText(String.valueOf(getZoneRadius()) + "m");

            updateMap();
            return true;
        }
    }


}
