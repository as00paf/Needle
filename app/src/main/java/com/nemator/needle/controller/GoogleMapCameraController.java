package com.nemator.needle.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.Needle;
import com.nemator.needle.views.ICustomMarker;
import com.nemator.needle.views.UserMarker;

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

    public void defaultCameraPositionning() {
        updateCameraOnPosition(config.getInitialLocation(), config.getDefaultLevel());
        positionInitialized = true;
    }

    public void updateCameraOnPosition(LatLng cameraPosition, float zoomLevel) {
        previousCameraTarget = getCameraTarget();

        CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
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

    public void zoom(LatLng newPosition) {
        LatLng scrollPosition = new LatLng(newPosition.latitude, newPosition.longitude);

        isFocussed = true;

        updateCameraOnPosition(scrollPosition, config.getZoomedLevel());
    }

    public void focus(LatLng newPosition, boolean lockScroll) {
        if(newPosition != null){
            LatLng scrollPosition = new LatLng(newPosition.latitude + config.getCorrectionY(), newPosition.longitude + config.getCorrectionX());

            isFocussed = true;
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(!lockScroll);

            float zoomLevel = config.getZoomedLevel() > getCameraZoom() ? config.getZoomedLevel() : getCameraZoom();
            updateCameraOnPosition(scrollPosition, zoomLevel);
        }
    }

    public void unfocus() {
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        isFocussed = false;

        updateCameraOnPosition(getCameraTarget(), config.getUnzoomedLevel());
    }

    public void unfocus(LatLng newPosition) {
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        isFocussed = false;

        updateCameraOnPosition(newPosition, config.getZoomedLevel());
    }

    private void onCameraChanged(CameraPosition cameraPosition) {
        if (!positionInitialized) {
            positionInitialized = true;
        }

        if (listener != null) {
            listener.onCameraChanged(cameraPosition);
        }
    }

    public void focusOnMyPosition() {
        isFocussed = false;

        //Users position
        LatLng currentPosition = null;
        final Location[] currentLocation = new Location[1];
        final GoogleApiClient apiClient = Needle.googleApiController.getGoogleApiClient();
        if (apiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return;
            }
            currentLocation[0] = LocationServices.FusedLocationApi
                    .getLastLocation(apiClient);

            if(currentLocation[0] == null){
                LocationRequest request = LocationRequest.create()
                        .setNumUpdates(1).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient,
                        request,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                currentLocation[0] = location;
                            }
                        });
            }
        }else{
            currentLocation[0] = mGoogleMap.getMyLocation();
        }

        if (currentLocation[0] != null){
            currentPosition = new LatLng(currentLocation[0].getLatitude()+config.getCorrectionY(), currentLocation[0].getLongitude()+config.getCorrectionX());
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

    public void focusOnMarkers(ICustomMarker... markers) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (ICustomMarker marker : markers) {
            builder.include(marker.getMarker().getPosition());
        }

        LatLngBounds bounds = builder.build();

        previousCameraTarget = getCameraTarget();

        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);

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

    //Interfaces
    public static interface CameraChangedListener{
        void onCameraChanged(CameraPosition cameraPosition);
    }
}
