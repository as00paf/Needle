package com.nemator.needle.tasks.createLocationSharing;

import android.content.Context;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;

public class CreateLocationSharingTaskParams {
    public Context context;
    public LocationSharingVO locationSharing;

    public CreateLocationSharingTaskParams(Context context, LocationSharingVO locationSharing){
        this.context = context;
        this.locationSharing = locationSharing;
    }
}
