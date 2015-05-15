package com.nemator.needle.tasks.activate;

import android.content.Context;

public class ActivateUserParams {

    public Context context;
    public String userId, haystackId;

    public ActivateUserParams(Context context, String userId, String haystackId){
        this.context = context;
        this.userId = userId;
        this.haystackId = haystackId;
    }
}
