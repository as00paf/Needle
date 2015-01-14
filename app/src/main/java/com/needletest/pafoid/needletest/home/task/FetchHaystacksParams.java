package com.needletest.pafoid.needletest.home.task;

import android.content.Context;
import android.widget.ProgressBar;

import java.lang.reflect.Method;

public class FetchHaystacksParams {
    public String userName;
    public String userId;
    public ProgressBar progressbar;
    public Context context;

    public FetchHaystacksParams(String userName, String userId, Context context, ProgressBar progressbar){
        this.userName = userName;
        this.userId = userId;
        this.progressbar = progressbar;
        this.context = context;
    }
}
