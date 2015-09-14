package com.nemator.needle.tasks.login;

import android.content.Context;
import android.widget.TextView;

import com.nemator.needle.models.vo.UserVO;

import java.lang.ref.WeakReference;

public class LoginTaskParams {
    public UserVO user;
    public Context context;
    public WeakReference<TextView> textView;

    public LoginTaskParams(Context context, UserVO user, WeakReference<TextView> textView){
        this.context = context;
        this.user = user;
        this.textView = textView;
    }

}
