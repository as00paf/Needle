package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

public class LoginResult extends AuthenticationResult{
    @SerializedName("haystackCount")
    private int haystackCount = 0;

    @SerializedName("locationSharingCount")
    private int locationSharingCount = 0;

    @SerializedName("type")
    private int type = 0;

    public LoginResult(int successCode, String message, UserVO user, int haystackCount, int locationSharingCount){
        super(successCode, message, user);

        this.haystackCount = haystackCount;
        this.locationSharingCount = locationSharingCount;
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
}
