package com.nemator.needle.fragments.haystacks.createHaystack;

import android.Manifest;
import android.animation.Animator;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.nemator.needle.tasks.getAutoCompleteResults.GetAutoCompleteResultsTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.GoogleMapDrawingUtils;
import com.nemator.needle.utils.PermissionManager;
import com.nemator.needle.fragments.SearchFragment;

public class CreateHaystackMapFragment extends CreateHaystackBaseFragment implements GoogleMapController.GoogleMapCallback {
    public static String TAG = "CreateHaystackMapFragment";

    //Children
    private TextView mRadiusLabel;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Menu menu;

    //Data
    private Boolean mIsMapMoveable = false;
    private Circle mCircle;
    private Polygon mPolygon;
    private Boolean mIsPolygonCircle = true;
    private Float mScaleFactor = 1.0f;
    private Location mCurrentLocation;
    private LatLng mCurrentPosition;
    private boolean initialized = false;

    //Objects
    private ScaleGestureDetector mScaleDetector;
    private GetAutoCompleteResultsTask autoCompleteTask;
    private GoogleApiClient mGoogleApiClient;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = Needle.googleApiController.getGoogleApiClient();
        if(mGoogleApiClient == null){
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.registerReceiver(apiConnectedReceiver, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
        }else{
            if(PermissionManager.getInstance(getActivity()).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
                addPlaceSuggestion();
            }else{
                PermissionManager.getInstance(getActivity()).requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.unregisterReceiver(this);

            addPlaceSuggestion();
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

        return rootView;
    }

    private void updateMap() {
        //Update user's marker and polygon
        LatLng position = mapController.getCurrentCameraTarget();
        if(position != null){
            if(mIsPolygonCircle){
                if(mCircle == null){
                    mCircle = GoogleMapDrawingUtils.drawCircle(googleMap, position, mScaleFactor, getResources(), mCircle);
                }else{
                    mCircle = GoogleMapDrawingUtils.updateCircle(mCircle, position, mScaleFactor);
                }
            }else{
                if(mPolygon == null){
                    mPolygon = GoogleMapDrawingUtils.drawPolygon(googleMap, position, mScaleFactor, getResources(), mPolygon);
                }else{
                    mPolygon = GoogleMapDrawingUtils.updatePolygon(mPolygon, position, mScaleFactor);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setupSearchView(menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_search:
                handleMenuSearch();
                break;
            case R.id.action_lock_map:
                if (mIsMapMoveable != true) {
                    mIsMapMoveable = true;
                    item.setIcon(getResources().getDrawable(R.drawable.ic_edit_location_white_24dp));
                } else {
                    mIsMapMoveable = false;
                    item.setIcon(getResources().getDrawable(R.drawable.ic_vpn_lock_white_24dp));
                }

                updateMap();
                break;
            case R.id.action_polygon:
                if (mIsPolygonCircle != true) {
                    if(mPolygon!=null){
                        mPolygon.remove();
                        mPolygon = null;
                    }

                    mIsPolygonCircle = true;
                    item.setIcon(getResources().getDrawable(R.drawable.square24));
                } else {
                    if(mCircle!=null){
                        mCircle.remove();
                        mCircle = null;
                    }

                    mIsPolygonCircle = false;
                    item.setIcon(getResources().getDrawable(R.drawable.circle24));
                }

                updateMap();
                break;
            case android.R.id.home:
                if(isSearchOpened) {
                    handleMenuSearch();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleMenuSearch() {
        int flag;
        if(isSearchOpened){
            closeSearchMenu();

            flag = MenuItem.SHOW_AS_ACTION_ALWAYS;
        } else {
            openSearchMenu();
            flag = MenuItem.SHOW_AS_ACTION_NEVER;
        }

        MenuItemCompat.setShowAsAction(menu.getItem(1), flag);
        MenuItemCompat.setShowAsAction(menu.getItem(2), flag);
    }

    private void setupSearchView(final Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchAction = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
            }
        });

        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            float originalHeight;

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                ActionBar action = ((CreateHaystackActivity) getActivity()).getSupportActionBar();
                mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white_24dp));
                action.setTitle(getResources().getString(R.string.app_name));
                action.setDisplayShowCustomEnabled(false);
                action.setDisplayShowTitleEnabled(true);
                action.setDisplayShowHomeEnabled(true);

                searchFragment.hide();
                isSearchOpened = false;

               /* Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MenuItemCompat.setShowAsAction(menu.getItem(1), MenuItem.SHOW_AS_ACTION_ALWAYS);
                        MenuItemCompat.setShowAsAction(menu.getItem(2), MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }
                }, 750);*/

                MenuItemCompat.setShowAsAction(menu.getItem(1), MenuItem.SHOW_AS_ACTION_ALWAYS);
                MenuItemCompat.setShowAsAction(menu.getItem(2), MenuItem.SHOW_AS_ACTION_ALWAYS);

                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;  // Return true to expand action view
            }
        });

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setMaxWidth(2000);
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
            if(location != null){
                closeSearchMenu();

                //mMapFragment.moveCameraTo(location);
            }
        }
    };

    private void openSearchMenu(){
        ActionBar action = ((AppCompatActivity) getActivity()).getSupportActionBar();
        action.setCustomView(R.layout.search_bar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                doSearch(newText);
                return false;
            }
        });

        //add the close icon
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        action.setTitle(getResources().getString(R.string.app_name));

        searchFragment.show(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                searchView.clearFocus();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //Show keyboard
                searchView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text), InputMethodManager.SHOW_FORCED);

                animation.removeListener(this);
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        isSearchOpened = true;
    }

    private void doSearch(String query) {
        Log.d(TAG, "doSearch::query : " + query);

        searchFragment.guessLocation(query, getCameraTargetBounds());
    }

    private void closeSearchMenu(){
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        ActionBar action = ((CreateHaystackActivity) getActivity()).getSupportActionBar();
        mSearchAction.collapseActionView();
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white_24dp));
        action.setTitle(getResources().getString(R.string.app_name));
        action.setDisplayShowCustomEnabled(false);
        action.setDisplayShowTitleEnabled(true);
        action.setDisplayShowHomeEnabled(true);

        searchFragment.hide();
        isSearchOpened = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mapController.setLocationUpdates(true);
            addPlaceSuggestion();
        }
    }

    private void addPlaceSuggestion(){
        if(mGoogleApiClient != null){
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                /*searchBox.clearSearchable();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    searchBox.addSearchable(new SearchResult(placeLikelihood.getPlace().getName().toString(), getResources().getDrawable(R.drawable.ic_action_place)));

                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }*/

                    likelyPlaces.release();
                }
            });
        }else{

        }
    }

    public void closeSearchResults() {
        //searchBox.toggleSearch();
    }

    public int getZoneRadius(){
        return (int) Math.round(mScaleFactor * 50.0);
    }

    public LatLng getPosition(){
        return mCurrentPosition;
    }

    public Boolean getIsCircle() {
        return mIsPolygonCircle;
    }

    public LatLngBounds getCameraTargetBounds() {
        return googleMap.getProjection().getVisibleRegion().latLngBounds;
    }

    @Override
    public void onMapInitialized() {
        updateMap();
    }

    public GoogleMapController getMapController() {
        return mapController;
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
