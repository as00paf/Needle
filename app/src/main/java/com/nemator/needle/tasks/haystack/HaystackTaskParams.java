package com.nemator.needle.tasks.haystack;

import android.content.Context;

import com.nemator.needle.models.vo.HaystackVO;

public class HaystackTaskParams {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_CREATE = "POST";
    public static final String TYPE_UPDATE = "PUT";
    public static final String TYPE_DELETE = "DELETE";

    public HaystackVO vo;
    public Context context;
    public String type;
    public int userId;

    //Constructor for get
    public HaystackTaskParams(Context context, String type, int userId){
        this.context = context;
        this.type = type;
        this.userId = userId;
    }

    public HaystackTaskParams(Context context, String type, HaystackVO vo){
        this.context = context;
        this.type = type;
        this.vo = vo;
    }

}
