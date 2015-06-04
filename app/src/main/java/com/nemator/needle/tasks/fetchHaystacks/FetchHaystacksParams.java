package com.nemator.needle.tasks.fetchHaystacks;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import java.lang.ref.WeakReference;

public class FetchHaystacksParams {
    public String userName;
    public String userId;
    public Context context;

    public FetchHaystacksParams(String userName, String userId, Context context){
        this.userName = userName;
        this.userId = userId;
        this.context = context;
    }
}
