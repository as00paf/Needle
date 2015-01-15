package com.needletest.pafoid.needletest.haystack.task;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class PostLocationParams{
    public String username, userId;
    public Location location;
    public Context context;
    public LatLng position;

    public PostLocationParams(Context context, String username, String userId, Location location, LatLng position){
        this.context = context;
        this.username = username;
        this.userId = userId;
        this.location = location;
        this.position = position;
    }
}