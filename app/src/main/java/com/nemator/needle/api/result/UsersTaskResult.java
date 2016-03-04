package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;

import java.util.ArrayList;

public class UsersTaskResult extends TaskResult{
    @SerializedName("users")
    private ArrayList<UserVO> users;

    public UsersTaskResult(int successCode, String message) {
        super(successCode, message);
    }

    public UsersTaskResult() {
    }

    public ArrayList<UserVO> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserVO> users) {
        this.users = users;
    }
}
