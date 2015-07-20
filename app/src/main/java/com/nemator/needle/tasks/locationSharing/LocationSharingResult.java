package com.nemator.needle.tasks.locationSharing;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LocationSharingResult {
    public ArrayList<LocationSharingVO> receivedLocationSharingList;
    public ArrayList<LocationSharingVO> sentLocationSharingList;
    public int successCode, receivedSuccessCode, sentSuccessCode;
    public String successMessage, receivedSuccessMessage, sentSuccessMessage;
    public LocationSharingVO vo;
    public WeakReference<LocationSharingListCardAdapter.LocationSharingCardViewHolder> viewHolderRef;

    public LocationSharingResult(){

    }

}
