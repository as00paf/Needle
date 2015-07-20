package com.nemator.needle.tasks.updateGCMRegId;

import android.content.Context;

/**
 * Created by Alex on 14/07/2015.
 */
public class UpdateGCMRegIdTaskParams {

    public Context context;
    public String userId;
    public String regId;

    public UpdateGCMRegIdTaskParams(Context context, String userId, String regId){
        this.context = context;
        this.userId = userId;
        this.regId = regId;
    }
}
