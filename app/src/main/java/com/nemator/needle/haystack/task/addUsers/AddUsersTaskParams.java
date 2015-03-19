package com.nemator.needle.haystack.task.addUsers;

import android.content.Context;

import com.nemator.needle.models.User;

import java.util.ArrayList;

public class AddUsersTaskParams {
    public ArrayList<User> users;
    public String haystackId;
    public Context context;

    public AddUsersTaskParams(Context context, String haystackId, ArrayList<User> users){
        this.context = context;
        this.haystackId = haystackId;
        this.users = users;
    }
}
