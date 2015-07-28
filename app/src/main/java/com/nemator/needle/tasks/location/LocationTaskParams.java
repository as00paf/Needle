package com.nemator.needle.tasks.location;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter.LocationSharingCardViewHolder;

import java.lang.ref.WeakReference;

public class LocationTaskParams {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_UPDATE = "PUT";
    public static final String TYPE_CANCEL = "DELETE";

    public String userId, haystackId;
    public String type;
    public Context context;
    public LatLng location;

    //Constructor for GET
    public LocationTaskParams(Context context, String type, String haystackId){
        this.context = context;
        this.type = type;

        this.haystackId = haystackId;
    }

    //Constructor for Update
    public LocationTaskParams(Context context, String type, String userId, Double latitude, Double longitude){
        this.context = context;
        this.type = type;

        this.userId = userId;
        this.location = new LatLng(latitude, longitude);
    }

    //Constructor for Update
    public LocationTaskParams(Context context, String type, String userId, LatLng location){
        this.context = context;
        this.type = type;

        this.userId = userId;
        this.location = location;
    }
}
