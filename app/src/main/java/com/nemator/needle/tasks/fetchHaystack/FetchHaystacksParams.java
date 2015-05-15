package com.nemator.needle.tasks.fetchHaystack;

import android.content.Context;
import android.widget.ProgressBar;

public class FetchHaystacksParams {
    public String userName;
    public String userId;
    public ProgressBar progressbar;
    public Context context;

    public FetchHaystacksParams(String userName, String userId, Context context, ProgressBar progressbar){
        this.userName = userName;
        this.userId = userId;
        this.progressbar = progressbar;
        this.context = context;
    }
}
