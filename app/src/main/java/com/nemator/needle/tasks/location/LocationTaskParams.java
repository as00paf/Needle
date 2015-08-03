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

    public String userId, haystackId, locationSharingId;
    public String type;
    public Context context;
    public LatLng location;

    public LocationTaskParams(){

    }

    //Constructor for GET haystacks
    public static LocationTaskParams newLocationHaystackTaskParams(Context context, String type, String haystackId){
        LocationTaskParams params = new LocationTaskParams();
        params.context = context;
        params.type = type;

        params.haystackId = haystackId;
        return params;
    }

    //Constructor for GET location sharing
    public static LocationTaskParams newLocationSharingLocationTaskParams(Context context, String type, String locationSharingId){
        LocationTaskParams params = new LocationTaskParams();
        params.context = context;
        params.type = type;

        params.locationSharingId = locationSharingId;
        return params;
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
