package com.nemator.needle.tasks.user;

import android.content.Context;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;

public class UserTaskParams {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_REGISTER = "POST";
    public static final String TYPE_UPDATE_GCM_ID = "PUT";
    public static final String TYPE_UNREGISTER = "DELETE";

    public UserVO vo;
    public Context context;
    public String type;

    //Constructor
    public UserTaskParams(Context context, String type, UserVO vo){
        this.context = context;
        this.type = type;
        this.vo = vo;
    }

}
