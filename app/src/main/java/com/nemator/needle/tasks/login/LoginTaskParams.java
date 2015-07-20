package com.nemator.needle.tasks.login;

import android.content.Context;

import com.nemator.needle.tasks.register.RegisterTaskParams;

public class LoginTaskParams extends RegisterTaskParams {
    Boolean rememberMe, verbose;

    public LoginTaskParams(String userName, String password, String regId, Context context, Boolean rememberMe, Boolean verbose){
        super(context, userName, password, regId);
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

    public LoginTaskParams(String userName, String password, Context context, Boolean rememberMe, Boolean verbose){
        super(context, userName, password);
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

}
