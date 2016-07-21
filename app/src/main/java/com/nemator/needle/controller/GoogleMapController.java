package com.nemator.needle.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.Needle;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.MarkerUtils;
import com.nemator.needle.utils.PermissionManager;
import com.nemator.needle.views.UserMarker;

public class GoogleMapController {

    private static final String TAG = "GoogleMapController";

    private Context context;

    private SupportMapFragment mapFragment;
    private GoogleMap mGoogleMap;

    private GoogleMapCameraControllerConfig config;
    private GoogleMapCallback callback;

    public GoogleMapCameraController cameraController;

    private GoogleMap.OnMarkerClickListener markerClickListener;

    public GoogleMapController(Context context, GoogleMapCameraControllerConfig config, GoogleMapCallback callback) {
        this.context = context;
        this.config = config;
        this.callback = callback;
    }

    public GoogleMapController(Context context, GoogleMapCameraControllerConfig config, GoogleMapCallback callback, GoogleMap.OnMarkerClickListener markerClickListener) {
        this.context = context;
        this.config = config;
        this.callback = callback;
        this.markerClickListener = markerClickListener;
    }

    public void initMap(final SupportMapFragment mapFragment){
        if(mapFragment == null){
            throw new Error(TAG + " - initMap(MapFragment)::MapFragment cannot be null");
        }

        this.mapFragment = mapFragment;

        MapsInitializer.initialize(context);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (googleMap != null) {
                    mGoogleMap = googleMap;
                    cameraController = new GoogleMapCameraController(context, mGoogleMap, config);

                    //Permission & Location
                    if(PermissionManager.getInstance(context).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(config.getMyLocationEnabled()) {
                            googleMap.setMyLocationEnabled(config.getMyLocationEnabled());
                        }

                        if(Needle.googleApiController.getLastKnownLocation() != null){
                            cameraController.zoom(Needle.googleApiController.getLastKnownLocation());
                        }
                    }else{
                        PermissionManager.getInstance(context).requestPermission((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION);
                    }

                    //UI Settings
                    mGoogleMap.getUiSettings().setZoomControlsEnabled(config.getZoomControlsEnabled());
                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(config.getMyLocationButtonEnabled());
                    mGoogleMap.getUiSettings().setTiltGesturesEnabled(config.getTiltGesturesEnabled());
                    mGoogleMap.getUiSettings().setZoomGesturesEnabled(config.getZoomGesturesEndabled());
                    mGoogleMap.getUiSettings().setScrollGesturesEnabled(config.getScrollGesturesEnabled());

                    //Map Type
                    googleMap.setMapType(config.getMapType());

                    //Click
                    if(markerClickListener != null){
                        mGoogleMap.setOnMarkerClickListener(markerClickListener);
                    }

                    if(callback != null)
                        callback.onMapInitialized(mGoogleMap);
                }
            }
        });
    }

    public void toggleMapControlGestures(Boolean enabled) {
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(enabled);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(enabled);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(enabled);
    }

    public LatLng getCurrentCameraTarget(){
        if(mGoogleMap != null){
            return mGoogleMap.getCameraPosition().target;
        }

        return null;
    }

    public void setLocationUpdates(Boolean update) {
        if (mGoogleMap != null) {
            if (update && PermissionManager.getInstance(context).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionManager.getInstance(context).requestPermission((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION);

                mGoogleMap.setMyLocationEnabled(update);
                return;
            }

            mGoogleMap.setMyLocationEnabled(false);
        }
    }

    public void removeListeners() {
        mGoogleMap.setOnMarkerClickListener(null);
        mGoogleMap.setOnCameraChangeListener(null);
        mGoogleMap.setOnMapClickListener(null);
        mGoogleMap.setOnMyLocationChangeListener(null);
    }

    public void snapshot(GoogleMap.SnapshotReadyCallback callback){
        mGoogleMap.snapshot(callback);
    }

    public GoogleMap getGoogleMap(){
        return mGoogleMap;
    }

    public void focusOnMyPosition() {
        cameraController.focusOnMyPosition();
    }

    public LatLngBounds getCurrentCameraTargetBounds() {
        return cameraController.getCameraTargetBounds();
    }

    public UserMarker createUserMarker(Context context, UserVO user, LatLng position, String snippet) {
        return MarkerUtils.createUserMarkerWithCircle(context, mGoogleMap, user, position, snippet);
    }

    public UserMarker createUserMarker(Context context, UserVO user, LatLng location, String snippet, int strokeColor, int shadeColor){
        return MarkerUtils.createUserMarkerWithCircle(context, mGoogleMap, user, location, snippet, strokeColor, shadeColor);
    }

    public void updateMarkersLocation(UserMarker marker, LatLng position) {
        marker.updateLocation(mGoogleMap, position);
    }

    public LatLng getCurrentPosition(){
        //Users position
        LatLng currentPosition = null;
        final Location[] currentLocation = new Location[1];
        final GoogleApiClient apiClient = Needle.googleApiController.getGoogleApiClient();
        if (apiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return null;
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

        return currentPosition;
    }

    public interface GoogleMapCallback{
        void onMapInitialized(GoogleMap map);
    }
}