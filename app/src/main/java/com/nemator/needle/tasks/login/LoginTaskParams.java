package com.nemator.needle.tasks.login;

import android.content.Context;

public class LoginTaskParams {
    public int socialNetworkId = 0;
    Boolean rememberMe, verbose;
    public String userName, password, gcmRegId, fbId;
    public Context context;

    public LoginTaskParams(String userName, String password, String regId, Context context, Boolean rememberMe, Boolean verbose){
        this.context = context;
        this.userName = userName;
        this.password = password;
        this.gcmRegId = regId;
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

    public LoginTaskParams(int socialNetworkId, String userName, String fbId, String regId, Context context, Boolean rememberMe, Boolean verbose){
        this.socialNetworkId = socialNetworkId;
        this.context = context;
        this.userName = userName;
        this.fbId = fbId;
        this.gcmRegId = regId;
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

    public LoginTaskParams(String userName, String password, Context context, Boolean rememberMe, Boolean verbose){
        this.userName = userName;
        this.password = password;
        this.context = context;
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

}
