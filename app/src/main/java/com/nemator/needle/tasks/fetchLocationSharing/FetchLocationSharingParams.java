package com.nemator.needle.tasks.fetchLocationSharing;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ProgressBar;

public class FetchLocationSharingParams {
    public String userId;
    public Context context;

    public FetchLocationSharingParams(String userId, Context context){
        this.userId = userId;
        this.context = context;
    }
}
