package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FriendshipVO implements Serializable, Parcelable {
    @SerializedName("userId")
    private int userId;

    @SerializedName("friendId")
    private int friendId;

    @SerializedName("status")
    private int status;

    @SerializedName("acceptDate")
    private String acceptDate;

    public FriendshipVO(){

    }

    public FriendshipVO(int userId, int friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public FriendshipVO(Parcel in){
        this.userId = in.readInt();
        this.friendId = in.readInt();
        this.status = in.readInt();
        this.acceptDate = in.readString();
    }

    public FriendshipVO(int userId, int friendId, int status, String acceptDate){
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.acceptDate = acceptDate;
    }

    public static final Creator<FriendshipVO> CREATOR = new Creator<FriendshipVO>() {
        @Override
        public FriendshipVO createFromParcel(Parcel in) {
            return new FriendshipVO(in);
        }

        @Override
        public FriendshipVO[] newArray(int size) {
            return new FriendshipVO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
     dest.writeInt(userId);
     dest.writeInt(friendId);
     dest.writeInt(status);
     dest.writeString(acceptDate);
    }

    //Getters/Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(String acceptDate) {
        this.acceptDate = acceptDate;
    }
}
