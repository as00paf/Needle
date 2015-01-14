package com.needletest.pafoid.needletest.authentication.task;

import android.content.Context;

public class LoginTaskParams extends RegisterTaskParams{
    Boolean rememberMe;

    public LoginTaskParams(String userName, String password, Context context, Boolean rememberMe){
        super(userName, password, context);
        this.rememberMe = rememberMe;
    }

}
