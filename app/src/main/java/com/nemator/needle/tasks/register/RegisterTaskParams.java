package com.nemator.needle.tasks.register;

import android.content.Context;

public class RegisterTaskParams {
    public String userName, password, gcmRegId;
    public Context context;

    public RegisterTaskParams(Context context, String userName, String password){
        this.userName = userName;
        this.password = password;
        this.gcmRegId = gcmRegId;
        this.context = context;
    }

    public RegisterTaskParams(Context context, String userName, String password, String gcmRegId){
        this.userName = userName;
        this.password = password;
        this.gcmRegId = gcmRegId;
        this.context = context;
    }
}
