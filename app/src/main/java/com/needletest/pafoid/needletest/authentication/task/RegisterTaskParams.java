package com.needletest.pafoid.needletest.authentication.task;

import android.content.Context;

public class RegisterTaskParams {
    public String userName, password;
    public Context context;

    public RegisterTaskParams(String userName, String password, Context context){
        this.userName = userName;
        this.password = password;
        this.context = context;
    }
}
