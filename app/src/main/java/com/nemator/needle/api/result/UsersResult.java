package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class UsersResult extends TaskResult{
    @SerializedName("users")
    private ArrayList<UserVO> users;

    public UsersResult(int successCode, String message) {
        super(successCode, message);
    }

    public UsersResult() {
    }

    public ArrayList<UserVO> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserVO> users) {
        this.users = users;
    }
}
