package com.nemator.needle.tasks.addUsers;

import android.content.Context;

import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class AddUsersTaskParams {
    public ArrayList<UserVO> users;
    public String haystackId;
    public Context context;

    public AddUsersTaskParams(Context context, String haystackId, ArrayList<UserVO> users){
        this.context = context;
        this.haystackId = haystackId;
        this.users = users;
    }
}
