package com.nemator.needle.fragments;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.PlacesRecyclerAdapter;
import com.nemator.needle.data.NeedleDBHelper;
import com.nemator.needle.models.vo.CustomPlace;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionManager;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements PlacesRecyclerAdapter.NearbyPlaceCardClickListener {

    private static final String TAG = "SearchFragment";
    //Views
    private View rootView;
    private RecyclerView recyclerView;
    private LinearLayoutManager recyclerLayoutManager;

    private int posY;
    private ArrayList<CustomPlace> nearbyPlaces;
    private ArrayList<CustomPlace> searchHistory;
    private PlacesRecyclerAdapter recyclerAdapter;

    private boolean isHistoryLoaded, isNearbyLoaded;

    public SearchFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "here");

                return false;
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
        recyclerLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        //Hide
        //TODO : Use XML animation
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        posY = size.y;

        rootView.setY(posY);

        initNearbyPlaces();
        initSearchHistory();

        return rootView;
    }
    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received API connected Intent");

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.unregisterReceiver(this);

            initNearbyPlaces();
        }
    };

    public void initNearbyPlaces() {
        if(PermissionManager.getInstance(getActivity()).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            Needle.googleApiController.getCurrentPlace(nearbyPlacesCallback, getActivity());
        }else{
            Log.d(TAG, "Permission denied");
            PermissionManager.getInstance(getActivity()).requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void initSearchHistory(){
        searchHistory = NeedleDBHelper.getInstance(getContext()).getSearchHistory();
        isHistoryLoaded = true;

        showSuggestions();
    }

    private ResultCallback<PlaceLikelihoodBuffer> nearbyPlacesCallback = new ResultCallback<PlaceLikelihoodBuffer>() {
        @Override
        public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
            Log.i(TAG, "Nearby places loaded : " + likelyPlaces.getStatus().getStatusMessage() +
                    " error code : " + likelyPlaces.getStatus().getStatusCode());

            nearbyPlaces = new ArrayList<>();

            for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                        placeLikelihood.getPlace().getName(),
                        placeLikelihood.getLikelihood()));

                CustomPlace place = new CustomPlace(placeLikelihood.getPlace().getId(),
                        placeLikelihood.getPlace().getName().toString(),
                         placeLikelihood.getPlace().getAddress().toString(),
                        placeLikelihood.getPlace().getLatLng());

                nearbyPlaces.add(place);
                if(nearbyPlaces.size() >= 5){
                    break;
                }
            }

            DataBufferUtils.freezeAndClose(likelyPlaces);

            isNearbyLoaded = true;
            showSuggestions();
        }
    };

    private void showSuggestions() {
        if(isNearbyLoaded && isHistoryLoaded){
            recyclerAdapter = new PlacesRecyclerAdapter(getActivity(), nearbyPlaces, searchHistory, this, null);
            recyclerView.setAdapter(recyclerAdapter);
        }else{
            Log.d(TAG, "Suggestions won't be shown");
        }
    }

    public void show(Animator.AnimatorListener listener){
        //showSuggestions();

        rootView.animate()
                .y(0)
                .setStartDelay(500)
                .setDuration(300)
                .setListener(listener)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public void hide(){
        rootView.animate()
                .y(posY)
                .setStartDelay(0)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    @Override
    public void onClick(CustomPlace place, boolean saveToHistory) {
        if(saveToHistory){
            NeedleDBHelper.getInstance(getContext()).insertSearchHistoryItem(place);
        }

        if(place.getLocation() == null || place.getLocation().equals(new LatLng(0,0))){
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(Needle.googleApiController.getGoogleApiClient(), place.getPlaceId());
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }else{
            showLocationOnMap(place);
        }
    }

    private void showLocationOnMap(CustomPlace place) {
        Intent intent = new Intent(getResources().getString(R.string.action_search_item_selected));
        intent.putExtra(getResources().getString(R.string.location), place.getLocation());
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }

            CustomPlace customPlace = new CustomPlace(places.get(0));
            showLocationOnMap(customPlace);

            places.release();
        }
    };

    public void guessLocation(String query, LatLngBounds bounds){
        if(recyclerAdapter == null){
            return;
        }

        if(TextUtils.isEmpty(query)){
            recyclerAdapter.setMode(PlacesRecyclerAdapter.DEFAULT_MODE);
        }else{
            recyclerAdapter.setMode(PlacesRecyclerAdapter.SEARCH_MODE);
            recyclerAdapter.setBounds(bounds);
            recyclerAdapter.getFilter().filter(query);
        }

    }
}
