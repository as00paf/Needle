package com.nemator.needle.tasks.locationSharing;

import android.content.Context;
import android.location.Location;

import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter.LocationSharingCardViewHolder;

import java.lang.ref.WeakReference;

public class LocationSharingParams {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_CREATE = "POST";
    public static final String TYPE_UPDATE = "PUT";
    public static final String TYPE_CANCEL = "DELETE";

    public String userId;
    public String type;
    public LocationSharingVO vo;
    public Context context;
    public String locationSharingId;
    public String regId;
    public WeakReference<LocationSharingCardViewHolder> viewHolderRef;

    //Constructor for GET
    public LocationSharingParams(Context context, String type, String userId){
        this.context = context;
        this.type = type;

        this.userId = userId;
    }

    //Constructor for CREATE
    public LocationSharingParams(Context context, String type, LocationSharingVO vo, String regId){
        this.context = context;
        this.type = type;

        this.vo = vo;
        this.regId = regId;
    }

    //Constructor for UPDATE
    public LocationSharingParams(Context context, String type, LocationSharingVO vo, WeakReference<LocationSharingCardViewHolder> viewHolderRef){
        this.context = context;
        this.type = type;

        this.vo = vo;
        this.viewHolderRef = viewHolderRef;
    }

    //Constructor for DELETE
    public LocationSharingParams(Context context, String type, LocationSharingVO vo){
        this.context = context;
        this.type = type;

        this.vo = vo;
    }
}
