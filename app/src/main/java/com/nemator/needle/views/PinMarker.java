package com.nemator.needle.views;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.utils.MarkerUtils;

public class PinMarker extends MarkerTarget implements ICustomMarker{

    private PinVO pin;

    public PinMarker(PinVO pin, Marker marker){
        super(marker);

        this.pin = pin;
    }

    public void updateLocation(GoogleMap map, LatLng location){
        if(marker != null){
            if(!marker.getPosition().equals(location)){
                MarkerUtils.animatePinMarker(map, marker, location, false);
            }

            this.pin.setLocation(location);
        }
    }

    public void showInfoWindow() {
        marker.showInfoWindow();
    }

    public void remove() {
        marker.remove();
    }

    //Getters/Setters
    public PinVO getPin() {
        return pin;
    }

    public void setPin(PinVO pin) {
        this.pin = pin;
    }

    public void setSnippet(String snippet){
        marker.setSnippet(snippet);
    }

    public void setTitle(String title) {
        marker.setTitle(title);
    }

    public LatLng getLocation() {
        return pin.getLocation();
    }
}

