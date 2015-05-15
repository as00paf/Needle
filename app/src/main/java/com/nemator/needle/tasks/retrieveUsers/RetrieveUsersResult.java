package com.nemator.needle.tasks.retrieveUsers;

import com.nemator.needle.models.vo.UserVO;

import org.json.JSONArray;

import java.util.ArrayList;

public class RetrieveUsersResult {
    public int successCode;
    public String message;
    public ArrayList<UserVO> userList;
    public JSONArray users;

}
