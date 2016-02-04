package com.nemator.needle.tasks.haystackUser;

import android.content.Context;

import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class HaystackUserTaskParams {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_ADD = "POST";
    public static final String TYPE_ACTIVATION = "PUT";
    public static final String TYPE_CANCEL = "DELETE";
    public Boolean isActive;

    public String userId, haystackId;
    public String type;
    public Context context;
    public ArrayList<UserVO> users;

    //Constructor for GET
    public HaystackUserTaskParams(Context context, String type, String userId, String haystackId){
        this.context = context;
        this.type = type;
        this.userId = userId;
        this.haystackId = haystackId;
    }

    //Constructor for CREATE
    public HaystackUserTaskParams(Context context, String type, String haystackId, ArrayList<UserVO> users){
        this.context = context;
        this.type = type;
        this.haystackId = haystackId;
        this.users = users;
    }

    //Constructor for UPDATE
    public HaystackUserTaskParams(Context context, String type, String userId, String haystackId, Boolean isActive){
        this.context = context;
        this.type = type;
        this.userId = userId;
        this.haystackId = haystackId;
        this.isActive = isActive;
    }

    //Constructor for DELETE
    public HaystackUserTaskParams(Context context, String type, LocationSharingVO vo){
        this.context = context;
        this.type = type;
    }
}
