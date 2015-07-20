package com.nemator.needle.models.vo;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;


public class LocationSharingVO implements Serializable, Parcelable {
    private int id;
    private String senderName;
    private int senderId;
    private String receiverName;
    private int receiverId;
    private String timeLimit;
    private Boolean shareBack;

    public LocationSharingVO(){

    }

    public LocationSharingVO(int id, String senderName, int senderId, String timeLimit, Boolean shareBack){
        this.id = id;
        this.senderName = senderName;
        this.senderId = senderId;
        this.timeLimit = timeLimit;
        this.shareBack = shareBack;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public LocationSharingVO(Parcel in){
        this.id = in.readInt();
        this.senderName = in.readString();
        this.senderId = in.readInt();
        this.receiverName = in.readString();
        this.receiverId = in.readInt();
        this.timeLimit = in.readString();
        this.shareBack = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(senderName);
        parcel.writeInt(senderId);
        parcel.writeString(receiverName);
        parcel.writeInt(receiverId);
        parcel.writeString(timeLimit);
        parcel.writeByte((byte) (shareBack ? 1 : 0));
    }

    public LocationSharingVO clone(){
        LocationSharingVO vo = new LocationSharingVO();
        vo.id = this.id;
        vo.senderId = this.senderId;
        vo.senderName = this.senderName;
        vo.receiverId = this.receiverId;
        vo.receiverName = this.receiverName;
        vo.timeLimit = this.timeLimit;
        vo.shareBack = this.shareBack;
        return vo;
    }

    //Getters/Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Boolean getShareBack() {
        return shareBack;
    }

    public void setShareBack(Boolean shareBack) {
        this.shareBack = shareBack;
    }
}
