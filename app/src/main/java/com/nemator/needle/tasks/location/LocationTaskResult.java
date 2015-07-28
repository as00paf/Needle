package com.nemator.needle.tasks.location;

import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationTaskResult {
    public int successCode;
    public String successMessage;
    public ArrayList<HashMap<String, Object>> locationList;

    public LocationTaskResult(){

    }

}
