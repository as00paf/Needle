package com.nemator.needle.tasks.fetchLocationSharing;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;

import java.util.ArrayList;

public class FetchLocationSharingResult {
    public ArrayList<LocationSharingVO> receivedLocationSharingList;
    public ArrayList<LocationSharingVO> sentLocationSharingList;
    public int successCode, receivedSuccessCode, sentSuccessCode;
    public String successMessage, receivedSuccessMessage, sentSuccessMessage;

    public FetchLocationSharingResult(){

    }

}
