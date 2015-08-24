package com.nemator.needle.tasks.login;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.AuthenticationResult;

public class LoginTaskResult extends AuthenticationResult{

    public int haystackCount = 0;
    public int locationSharingCount = 0;
    public int type = 0;

    public LoginTaskResult(int successCode, String message, UserVO user, int haystackCount, int locationSharingCount){
        super(successCode, message, user);

        this.haystackCount = haystackCount;
        this.locationSharingCount = locationSharingCount;
    }

    public LoginTaskResult(){}
}
