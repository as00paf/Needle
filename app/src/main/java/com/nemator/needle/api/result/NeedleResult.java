package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.NeedleVO;

import java.util.ArrayList;

public class NeedleResult extends TaskResult {

    @SerializedName("locationSharing")
    private NeedleVO locationSharing;

    @SerializedName("locationSharings")
    private ArrayList<NeedleVO> locationSharings;

    @SerializedName("sent_success")
    protected int sentSuccessCode;

    @SerializedName("sent_message")
    protected String sentMessage;

    @SerializedName("received_success")
    protected int receivedSuccessCode;

    @SerializedName("received_message")
    protected String receivedMessage;

    @SerializedName("received")
    private ArrayList<NeedleVO> receivedLocationSharings = null;

    @SerializedName("sent")
    private ArrayList<NeedleVO> sentLocationSharings = null;

    public NeedleResult() {
    }

    public NeedleResult(int successCode, String message, NeedleVO locationSharing, ArrayList<NeedleVO> locationSharings, ArrayList<NeedleVO> receivedLocationSharings, ArrayList<NeedleVO> sentLocationSharings) {
        super(successCode, message);
        this.locationSharing = locationSharing;
        this.locationSharings = locationSharings;
        this.receivedLocationSharings = receivedLocationSharings;
        this.sentLocationSharings = sentLocationSharings;
    }

    //Getters/Setters
    public NeedleVO getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(NeedleVO locationSharing) {
        this.locationSharing = locationSharing;
    }

    public ArrayList<NeedleVO> getLocationSharings() {
        return locationSharings;
    }

    public void setLocationSharings(ArrayList<NeedleVO> locationSharings) {
        this.locationSharings = locationSharings;
    }

    public ArrayList<NeedleVO> getReceivedLocationSharings() {
        return receivedLocationSharings;
    }

    public void setReceivedLocationSharings(ArrayList<NeedleVO> receivedLocationSharings) {
        this.receivedLocationSharings = receivedLocationSharings;
    }

    public ArrayList<NeedleVO> getSentLocationSharings() {
        return sentLocationSharings;
    }

    public void setSentLocationSharings(ArrayList<NeedleVO> sentLocationSharings) {
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
