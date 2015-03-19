package com.nemator.needle.haystack.task.leaveHaystack;

import android.content.Context;

public class LeaveHaystackParams {

    public Context context;
    public String userId, haystackId;

    public LeaveHaystackParams(Context context, String userId, String haystackId){
        this.context = context;
        this.userId = userId;
        this.haystackId = haystackId;
    }
}
