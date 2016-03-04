package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.tasks.TaskResult;

public class UserRegistrationResult extends TaskResult{

    @SerializedName("userId")
    private int userId;

    @SerializedName("loginType")
    private int loginType;

    @SerializedName("socialNetworkUserId")
    private String socialNetworkUserId;

    public UserRegistrationResult() {
        super();
    }

    public UserRegistrationResult(int successCode, String message, int userId, int loginType, String socialNetworkUserId) {
        super(successCode, message);

        this.userId = userId;
        this.loginType = loginType;
        this.socialNetworkUserId = socialNetworkUserId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getSocialNetworkUserId() {
        return socialNetworkUserId;
    }

    public void setSocialNetworkUserId(String socialNetworkUserId) {
        this.socialNetworkUserId = socialNetworkUserId;
    }
}
