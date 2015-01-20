package com.needletest.pafoid.needletest.haystack.task;

import android.content.Context;

public class DeactivateUserParams {

    public Context context;
    public String userId, haystackId;

    public DeactivateUserParams(Context context, String userId, String haystackId){
        this.context = context;
        this.userId = userId;
        this.haystackId = haystackId;
    }
}
