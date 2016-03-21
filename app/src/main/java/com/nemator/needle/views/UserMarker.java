package com.nemator.needle.views;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.MarkerUtils;

public final class UserMarker extends MarkerTarget{

    private UserVO user;
    private LatLng location;
    private Circle circle;

    public UserMarker(UserVO user, Marker marker){
        super(marker);

        this.user = user;
    }

    public UserMarker(UserVO user, Marker marker, Circle circle, LatLng location){
        super(marker);

        this.user = user;
        this.circle = circle;
        this.location = location;
    }

    public void updateLocation(GoogleMap map, LatLng location){
        if(marker != null && circle != null){
            if(!marker.getPosition().equals(location) || !circle.getCenter().equals(location)){
                MarkerUtils.animateUserMarker(map, marker, circle, location, false);
            }

            this.location = location;
        }
    }

    //Getters/Setters
    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }
}

