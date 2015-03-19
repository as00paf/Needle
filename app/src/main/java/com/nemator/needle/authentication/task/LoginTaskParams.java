package com.nemator.needle.authentication.task;

import android.content.Context;

public class LoginTaskParams extends RegisterTaskParams{
    Boolean rememberMe, verbose;

    public LoginTaskParams(String userName, String password, Context context, Boolean rememberMe, Boolean verbose){
        super(userName, password, context);
        this.rememberMe = rememberMe;
        this.verbose = verbose;
    }

}
