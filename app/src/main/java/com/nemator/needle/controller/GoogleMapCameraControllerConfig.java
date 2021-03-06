package com.nemator.needle.controller;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Alex on 09/12/2015.
 */
public class GoogleMapCameraControllerConfig {

    public static final int DEFAULT_ZOOMED_LEVEL = 18;
    public static final int DEFAULT_UNZOOMED_LEVEL = 17;
    public static final int DEFAULT_MIN_ZOOM_LEVEL = 15;
    public static final LatLng DEFAULT_INITIAL_COORDINATES = new LatLng(45.5079858,-73.5734997);//Montreal
    public static final LatLngBounds DEFAULT_INITIAL_BOUNDS = new LatLngBounds(new LatLng(45.345920, -74.004539), new LatLng(45.707086, -73.424608));//Montreal

    private int zoomedLevel = DEFAULT_ZOOMED_LEVEL;
    private int unzoomedLevel = DEFAULT_UNZOOMED_LEVEL;
    private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private int defaultLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private float correctionX = 0.0f; //Longitude
    private float correctionY = 0.0f; //Latitude
    private LatLng initialLocation = DEFAULT_INITIAL_COORDINATES;
    private LatLngBounds initialBounds = DEFAULT_INITIAL_BOUNDS;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;

    private Boolean zoomControlsEnabled = false;
    private Boolean myLocationButtonEnabled = false;
    private Boolean tiltGesturesEnabled = false;
    private Boolean zoomGesturesEndabled = true;
    private Boolean scrollGesturesEnabled = true;
    private Boolean myLocationEnabled = true;

    public static GoogleMapCameraControllerConfig create(){
        return new GoogleMapCameraControllerConfig();
    }

    public GoogleMapCameraControllerConfig() {
    }

    public GoogleMapCameraControllerConfig(int zoomedLevel, int unzoomedLevel, int minZoomLevel, int defaultLevel) {
        this.zoomedLevel = zoomedLevel;
        this.unzoomedLevel = unzoomedLevel;
        this.minZoomLevel = minZoomLevel;
        this.defaultLevel = defaultLevel;
    }

    public GoogleMapCameraControllerConfig(int zoomedLevel, int unzoomedLevel, int minZoomLevel, int defaultLevel, float correctionX) {
        this.zoomedLevel = zoomedLevel;
        this.unzoomedLevel = unzoomedLevel;
        this.minZoomLevel = minZoomLevel;
        this.defaultLevel = defaultLevel;
        this.correctionX = correctionX;
    }

    public GoogleMapCameraControllerConfig(int zoomedLevel, int unzoomedLevel, int minZoomLevel, int defaultLevel, LatLng initialLocation) {
        this.zoomedLevel = zoomedLevel;
        this.unzoomedLevel = unzoomedLevel;
        this.minZoomLevel = minZoomLevel;
        this.defaultLevel = defaultLevel;
        this.initialLocation = initialLocation;
    }

    public GoogleMapCameraControllerConfig(int zoomedLevel, int unzoomedLevel, int minZoomLevel, int defaultLevel, LatLng initialLocation, LatLngBounds initialBounds) {
        this.zoomedLevel = zoomedLevel;
        this.unzoomedLevel = unzoomedLevel;
        this.minZoomLevel = minZoomLevel;
        this.defaultLevel = defaultLevel;
        this.initialLocation = initialLocation;
        this.initialBounds = initialBounds;
    }

    public LatLngBounds getInitialBounds() {
        return initialBounds;
    }

    public GoogleMapCameraControllerConfig setInitialBounds(LatLngBounds initialBounds) {
        this.initialBounds = initialBounds;
        return this;
    }

    public LatLng getInitialLocation() {
        return initialLocation;
    }

    public GoogleMapCameraControllerConfig setInitialLocation(LatLng initialLocation) {
        this.initialLocation = initialLocation;
        return this;
    }

    public int getDefaultLevel() {
        return defaultLevel;
    }

    public GoogleMapCameraControllerConfig setDefaultLevel(int defaultLevel) {
        this.defaultLevel = defaultLevel;
        return this;
    }

    public int getMinZoomLevel() {
        return minZoomLevel;
    }

    public GoogleMapCameraControllerConfig setMinZoomLevel(int minZoomLevel) {
        this.minZoomLevel = minZoomLevel;
        return this;
    }

    public int getUnzoomedLevel() {
        return unzoomedLevel;
    }

    public GoogleMapCameraControllerConfig setUnzoomedLevel(int unzoomedLevel) {
        this.unzoomedLevel = unzoomedLevel;
        return this;
    }

    public int getZoomedLevel() {
        return zoomedLevel;
    }

    public GoogleMapCameraControllerConfig setZoomedLevel(int zoomedLevel) {
        this.zoomedLevel = zoomedLevel;
        return this;
    }

    public float getCorrectionX() {
        return correctionX;
    }

    public float getCorrectionY() {
        return correctionY;
    }

    public GoogleMapCameraControllerConfig setCorrectionY(float correctionY) {
        this.correctionY = correctionY;
        return this;
    }

    public GoogleMapCameraControllerConfig setCorrectionX(float correctionX) {
        this.correctionX = correctionX;
        return this;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public Boolean getZoomControlsEnabled() {
        return zoomControlsEnabled;
    }

    public GoogleMapCameraControllerConfig setZoomControlsEnabled(Boolean zoomControlsEnabled) {
        this.zoomControlsEnabled = zoomControlsEnabled;
        return this;
    }

    public Boolean getMyLocationButtonEnabled() {
        return myLocationButtonEnabled;
    }

    public GoogleMapCameraControllerConfig setMyLocationButtonEnabled(Boolean myLocationButtonEnabled) {
        this.myLocationButtonEnabled = myLocationButtonEnabled;
        return this;
    }

    public Boolean getTiltGesturesEnabled() {
        return tiltGesturesEnabled;
    }

    public GoogleMapCameraControllerConfig setTiltGesturesEnabled(Boolean tiltGesturesEnabled) {
        this.tiltGesturesEnabled = tiltGesturesEnabled;
        return this;
    }

    public Boolean getZoomGesturesEndabled() {
        return zoomGesturesEndabled;
    }

    public GoogleMapCameraControllerConfig setZoomGesturesEndabled(Boolean zoomGesturesEndabled) {
        this.zoomGesturesEndabled = zoomGesturesEndabled;
        return this;
    }

    public Boolean getScrollGesturesEnabled() {
        return scrollGesturesEnabled;
    }

    public GoogleMapCameraControllerConfig setScrollGesturesEnabled(Boolean scrollGesturesEnabled) {
        this.scrollGesturesEnabled = scrollGesturesEnabled;
        return this;
    }

    public Boolean getMyLocationEnabled() {
        return myLocationEnabled;
    }

    public GoogleMapCameraControllerConfig setMyLocationEnabled(Boolean myLocationEnabled) {
        this.myLocationEnabled = myLocationEnabled;
        return this;
    }
}
