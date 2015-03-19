package com.nemator.needle.haystack.task.postLocation;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class PostLocationParams{
    public String username, userId;
    public Location location;
    public Context context;
    public LatLng position;
    public Boolean verbose;

    public PostLocationParams(Context context, String username, String userId, Location location, LatLng position, Boolean verbose){
        this.context = context;
        this.username = username;
        this.userId = userId;
        this.location = location;
        this.position = position;
        this.verbose = verbose;
    }
}