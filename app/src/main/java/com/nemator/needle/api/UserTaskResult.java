package com.nemator.needle.api;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;

import java.util.ArrayList;

public class UserTaskResult extends TaskResult{
    @SerializedName("user")
    private UserVO user;

    public UserTaskResult(int successCode, String message, UserVO user) {
        super(successCode, message);
        this.user = user;
    }

    public UserTaskResult() {
    }

    public UserVO getUsers() {
        return user;
    }

    public void setUsers(UserVO user) {
        this.user = user;
    }
}
