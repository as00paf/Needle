package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

import java.io.Serializable;

public class LoginResult extends AuthenticationResult implements Serializable{
    @SerializedName("haystackCount")
    private int haystackCount = 0;

    @SerializedName("locationSharingCount")
    private int locationSharingCount = 0;

    @SerializedName("notificationCount")
    private int notificationCount = 0;

    @SerializedName("type")
    private int type = 0;

    public LoginResult(int successCode, String message, UserVO user, int haystackCount, int locationSharingCount, int notificationCount){
        super(successCode, message, user);

        this.haystackCount = haystackCount;
        this.locationSharingCount = locationSharingCount;
        this.notificationCount = notificationCount;
    }

    public LoginResult(){
        super(-1, "", null);
    }

    public int getHaystackCount() {
        return haystackCount;
    }

    public void setHaystackCount(int haystackCount) {
        this.haystackCount = haystackCount;
    }

    public int getLocationSharingCount() {
        return locationSharingCount;
    }

    public void setLocationSharingCount(int locationSharingCount) {
        this.locationSharingCount = locationSharingCount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
