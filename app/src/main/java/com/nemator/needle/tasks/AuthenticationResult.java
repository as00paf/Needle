package com.nemator.needle.tasks;

import com.nemator.needle.models.vo.UserVO;

public class AuthenticationResult {
    public int successCode = 0;
    public String message;
    public UserVO user;

    public AuthenticationResult(int successCode, String message, UserVO user){
        this.successCode = successCode;
        this.message = message;
        this.user = user;
    }

    public AuthenticationResult(){

    }
}
