package com.nemator.needle.tasks.login;

import android.content.Context;

import com.nemator.needle.models.vo.UserVO;

public class LoginTaskParams {
    public UserVO user;
    public Context context;

    public LoginTaskParams(Context context, UserVO user){
        this.context = context;
        this.user = user;
    }

}
