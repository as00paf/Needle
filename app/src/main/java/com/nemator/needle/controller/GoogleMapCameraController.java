package com.nemator.needle.controller;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Alex on 09/12/2015.
 */
public class GoogleMapCameraController {
    private static final String TAG = "CameraController";

    private Context context;
    private GoogleMap mGoogleMap;
    private GoogleMapCameraControllerConfig config;
    private CameraChangedListener listener;

    private boolean isFocussed = false;
    private boolean isVeryHigh = true;
    private boolean isHigh = true;
    private boolean positionInitialized = false;
    private LatLng previousCameraTarget;

    public GoogleMapCameraController(Context context, GoogleMap mGoogleMap, GoogleMapCameraControllerConfig config) {
        this.context = context;
        this.mGoogleMap = mGoogleMap;
        this.config = config;

        init();
    }

    private void init() {
        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                onCameraChanged(cameraPosition);
            }
        });
    }

    public void defaultCameraPositionning(){
        updateCameraOnPosition(config.getInitialLocation(), config.getDefaultLevel());
        positionInitialized = true;
    }

    public void updateCameraOnPosition(LatLng cameraPosition, float zoomLevel){
        previousCameraTarget = getCameraTarget();

        CameraUpdate update =  CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(cameraPosition)
                .zoom(zoomLevel)
                .bearing(0)
                .build());

        mGoogleMap.animateCamera(update, 300, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                onCameraChanged(null);
            }

            @Override
            public void onCancel() {
                onCameraChanged(null);
            }
        });
    }

    public void zoom(LatLng newPosition){
        LatLng scrollPosition = new LatLng(newPosition.latitude, newPosition.longitude);

        isFocussed = true ;

        updateCameraOnPosition(scrollPosition, config.getZoomedLevel());
    }

    public void focus(LatLng newPosition){
        LatLng scrollPosition = new LatLng(newPosition.latitude + config.getCorrectionY(), newPosition.longitude + config.getCorrectionX());

        isFocussed = true ;
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);

        updateCameraOnPosition(scrollPosition, config.getZoomedLevel());
    }

    public void unfocus(){
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        isFocussed = false ;

        updateCameraOnPosition(getCameraTarget(), config.getUnzoomedLevel());
    }

    public void unfocus(LatLng newPosition) {
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        isFocussed = false ;

        updateCameraOnPosition(newPosition, config.getZoomedLevel());
    }

    private void onCameraChanged(CameraPosition cameraPosition){
        if(!positionInitialized){
            positionInitialized = true;
        }

        if(listener != null){
            listener.onCameraChanged(cameraPosition);
        }
    }

    public void focusOnMyPosition() {
        isFocussed = false ;
        Location currentLocation = mGoogleMap.getMyLocation();
        LatLng currentPosition = null;
        if (currentLocation != null){
            currentPosition = new LatLng(currentLocation.getLatitude()+config.getCorrectionY(), currentLocation.getLongitude()+config.getCorrectionX());
        }

        if (currentPosition != null) {
            updateCameraOnPosition(currentPosition, config.getUnzoomedLevel());
        }
    }

    //Getters/Setters
    public float getCameraZoom(){
        return mGoogleMap.getCameraPosition().zoom;
    }

    public LatLng getCameraTarget(){
        return mGoogleMap.getCameraPosition().target;
    }

    public LatLngBounds getCameraTargetBounds(){
        return mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
    }

    public void setCameraChangedListener(CameraChangedListener listener) {
        this.listener = listener;
    }

    //Interfaces
    public interface CameraChangedListener{
        void onCameraChanged(CameraPosition cameraPosition);
    }
}
