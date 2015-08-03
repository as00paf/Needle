package com.nemator.needle.tasks.login;

import android.content.Context;

public class LoginTaskParams {
    Boolean rememberMe, verbose;
    public String userName, password, gcmRegId;
    public Context context;

    public LoginTaskParams(String userName, String password, String regId, Context context, Boolean rememberMe, Boolean verbose){
        this.context = context;
        this.userName = userName;
        this.password = password;
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
