package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

import java.io.Serializable;

public class AuthenticationResult extends TaskResult implements Serializable{
    @SerializedName("user")
    private UserVO user;

    public AuthenticationResult(int successCode, String message, UserVO user){
        super(successCode, message);
        this.user = user;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
