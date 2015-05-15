package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserVO implements Serializable, Parcelable{

    private int userId;
    private String userName, pictureURL;

    public UserVO(){

    }

    public UserVO(Parcel in){
        userId = in.readInt();
        userName = in.readString();
        pictureURL = in.readString();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeString(pictureURL);
    }

    public static final Parcelable.Creator<UserVO> CREATOR = new Parcelable.Creator<UserVO>() {

        @Override
        public UserVO createFromParcel(Parcel source) {
            return new UserVO(source);
        }

        @Override
        public UserVO[] newArray(int size) {
            return new UserVO[size];
        }
    };
}
