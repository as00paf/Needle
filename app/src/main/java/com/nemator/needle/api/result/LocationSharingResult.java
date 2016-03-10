package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.LocationSharingVO;

import java.util.ArrayList;

public class LocationSharingResult extends TaskResult {

    @SerializedName("locationSharing")
    private LocationSharingVO locationSharing;

    @SerializedName("locationSharings")
    private ArrayList<LocationSharingVO> locationSharings;

    @SerializedName("sent_success")
    protected int sentSuccessCode;

    @SerializedName("sent_message")
    protected String sentMessage;

    @SerializedName("received_success")
    protected int receivedSuccessCode;

    @SerializedName("received_message")
    protected String receivedMessage;

    @SerializedName("received")
    private ArrayList<LocationSharingVO> receivedLocationSharings = null;

    @SerializedName("sent")
    private ArrayList<LocationSharingVO> sentLocationSharings = null;

    public LocationSharingResult() {
    }

    public LocationSharingResult(int successCode, String message, LocationSharingVO locationSharing, ArrayList<LocationSharingVO> locationSharings, ArrayList<LocationSharingVO> receivedLocationSharings, ArrayList<LocationSharingVO> sentLocationSharings) {
        super(successCode, message);
        this.locationSharing = locationSharing;
        this.locationSharings = locationSharings;
        this.receivedLocationSharings = receivedLocationSharings;
        this.sentLocationSharings = sentLocationSharings;
    }

    //Getters/Setters
    public LocationSharingVO getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(LocationSharingVO locationSharing) {
        this.locationSharing = locationSharing;
    }

    public ArrayList<LocationSharingVO> getLocationSharings() {
        return locationSharings;
    }

    public void setLocationSharings(ArrayList<LocationSharingVO> locationSharings) {
        this.locationSharings = locationSharings;
    }

    public ArrayList<LocationSharingVO> getReceivedLocationSharings() {
        return receivedLocationSharings;
    }

    public void setReceivedLocationSharings(ArrayList<LocationSharingVO> receivedLocationSharings) {
        this.receivedLocationSharings = receivedLocationSharings;
    }

    public ArrayList<LocationSharingVO> getSentLocationSharings() {
        return sentLocationSharings;
    }

    public void setSentLocationSharings(ArrayList<LocationSharingVO> sentLocationSharings) {
        this.sentLocationSharings = sentLocationSharings;
    }

    public int getSentSuccessCode() {
        return sentSuccessCode;
    }

    public void setSentSuccessCode(int sentSuccessCode) {
        this.sentSuccessCode = sentSuccessCode;
    }

    public String getSentMessage() {
        return sentMessage;
    }

    public void setSentMessage(String sentMessage) {
        this.sentMessage = sentMessage;
    }

    public int getReceivedSuccessCode() {
        return receivedSuccessCode;
    }

    public void setReceivedSuccessCode(int receivedSuccessCode) {
        this.receivedSuccessCode = receivedSuccessCode;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }
}
