package com.nemator.needle.haystack.task.retrieveUsers;

import com.nemator.needle.models.User;

import org.json.JSONArray;

import java.util.ArrayList;

public class RetrieveUsersResult {
    public int successCode;
    public String message;
    public ArrayList<User> userList;
    public JSONArray users;

}
