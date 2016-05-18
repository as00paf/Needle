package com.nemator.needle.tasks.db.addPostLocationRequest;

import android.content.Context;

public class AddPostLocationRequestParams {

    public int id = -1;
    public int type;
    public int posterId;
    public String expiration;
    public Context context;
    public String itemId;

    public AddPostLocationRequestParams(Context context, int type, String expiration, int posterId, String itemId){
        this.context = context;
        this.type = type;
        this.posterId = posterId;
        this.expiration = expiration;
        this.itemId = itemId;
    }

    public AddPostLocationRequestParams(Context context, int id, int type, String expiration, int posterId, String itemId){
        this.context = context;
        this.id = id;
        this.type = type;
        this.posterId = posterId;
        this.expiration = expiration;
        this.itemId = itemId;
    }
}
