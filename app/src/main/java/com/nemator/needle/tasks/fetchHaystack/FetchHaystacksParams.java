package com.nemator.needle.tasks.fetchHaystack;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ProgressBar;

public class FetchHaystacksParams {
    public String userName;
    public String userId;
    public SwipeRefreshLayout refreshLayout;
    public Context context;

    public FetchHaystacksParams(String userName, String userId, Context context, SwipeRefreshLayout refreshLayout){
        this.userName = userName;
        this.userId = userId;
        this.refreshLayout = refreshLayout;
        this.context = context;
    }
}
