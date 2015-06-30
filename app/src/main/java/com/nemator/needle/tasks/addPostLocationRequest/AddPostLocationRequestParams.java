package com.nemator.needle.tasks.addPostLocationRequest;

import android.content.Context;

public class AddPostLocationRequestParams {
    public int type;
    public int posterId;
    public String expiration;
    public Context context;
    public long rowId;

    public AddPostLocationRequestParams(Context context, int type, String expiration, int posterId){
        this.context = context;
        this.type = type;
        this.posterId = posterId;
        this.expiration = expiration;
    }
}
