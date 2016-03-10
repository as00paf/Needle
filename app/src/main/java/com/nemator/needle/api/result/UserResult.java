package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

public class UserResult extends TaskResult{
    @SerializedName("user")
    private UserVO user;

    public UserResult(int successCode, String message, UserVO user) {
        super(successCode, message);
        this.user = user;
    }

    public UserResult() {
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
