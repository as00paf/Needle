package com.nemator.needle.tasks.fetchLocationSharing;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ProgressBar;

public class FetchLocationSharingParams {
    public String userId;
    public SwipeRefreshLayout refreshLayout;
    public Context context;

    public FetchLocationSharingParams(String userId, Context context, SwipeRefreshLayout refreshLayout){
        this.userId = userId;
        this.refreshLayout = refreshLayout;
        this.context = context;
    }
}
