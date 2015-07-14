package com.nemator.needle.tasks.cancelLocationSharing;

import android.content.Context;
import android.location.Location;

import com.nemator.needle.models.vo.LocationSharingVO;

public class CancelLocationSharingTaskParams {
    public Context context;
    public LocationSharingVO data;

    public CancelLocationSharingTaskParams(Context context, LocationSharingVO data){
        this.context = context;
        this.data = data;
    }
}
