package com.nemator.needle.tasks.db.addPostLocationRequest;

import android.content.Context;

public class AddPostLocationRequestParams {
    public int type;
    public int posterId;
    public String expiration;
    public Context context;
    public String itemId;
    public long rowId;

    public AddPostLocationRequestParams(Context context, int type, String expiration, int posterId, String itemId){
        this.context = context;
        this.type = type;
        this.posterId = posterId;
        this.expiration = expiration;
        this.itemId = itemId;
    }
}
