package com.nemator.needle.tasks.cancelLocationSharing;

import android.location.Location;

import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.TaskResult;

public class CancelLocationSharingTaskResult extends TaskResult{
    public LocationSharingVO data;

    public CancelLocationSharingTaskResult(int successCode, String message){
        super(successCode, message);
    }

    public CancelLocationSharingTaskResult(int successCode, String message, LocationSharingVO data){
        super(successCode, message);
        this.data = data;
    }

    public CancelLocationSharingTaskResult(){}
}
