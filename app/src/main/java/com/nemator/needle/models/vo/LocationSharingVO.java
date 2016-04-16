package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationSharingVO implements Serializable, Parcelable {
    @SerializedName("id")
    private int id;

    @SerializedName("sender")
    private UserVO sender;

    @SerializedName("receiver")
    private UserVO receiver;

    @SerializedName("timeLimit")
    private String timeLimit;

    @SerializedName("shareBack")
    private Boolean shareBack = false;

    public LocationSharingVO(){

    }

    public LocationSharingVO(int id, String timeLimit, Boolean shareBack){
        this.id = id;
        this.timeLimit = timeLimit;
        this.shareBack = shareBack;
    }

    public static final Creator<LocationSharingVO> CREATOR = new Creator<LocationSharingVO>() {
        @Override
        public LocationSharingVO createFromParcel(Parcel in) {
            return new LocationSharingVO(in);
        }

        @Override
        public LocationSharingVO[] newArray(int size) {
            return new LocationSharingVO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public LocationSharingVO(Parcel in){
        this.id = in.readInt();
        this.timeLimit = in.readString();
        this.shareBack = in.readByte() != 0;
        this.sender = (UserVO) in.readParcelable(UserVO.class.getClassLoader());
        this.receiver = (UserVO) in.readParcelable(UserVO.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(timeLimit);
        parcel.writeByte((byte) (shareBack ? 1 : 0));
        parcel.writeParcelable(sender, i);
        parcel.writeParcelable(receiver, i);
    }

    public LocationSharingVO clone(){
        LocationSharingVO vo = new LocationSharingVO();
        vo.id = this.id;
        vo.timeLimit = this.timeLimit;
        vo.shareBack = this.shareBack;
        vo.sender = this.sender;
        vo.receiver = this.receiver;
        return vo;
    }

    //Getters/Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserVO getSender() {
        return sender;
    }

    public void setSender(UserVO sender) {
        this.sender = sender;
    }

    public UserVO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserVO receiver) {
        this.receiver = receiver;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Boolean isSharedBack() {
        return shareBack;
    }

    public void setShareBack(Boolean shareBack) {
        this.shareBack = shareBack;
    }
}
