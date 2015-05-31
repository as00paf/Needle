package com.nemator.needle.tasks.shareLocation;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class ShareLocationParams {
    public String username, userId;
    public Context context;
    public LatLng position;
    public Boolean verbose;

    public ShareLocationParams(Context context, String username, String userId, Location location, LatLng position, Boolean verbose){
        this.context = context;
        this.username = username;
        this.userId = userId;
        this.position = position;
        this.verbose = verbose;
    }
}