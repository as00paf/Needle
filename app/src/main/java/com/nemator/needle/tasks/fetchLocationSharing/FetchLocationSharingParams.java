package com.nemator.needle.tasks.fetchLocationSharing;

import android.content.Context;
import android.widget.ProgressBar;

public class FetchLocationSharingParams {
    public String userId;
    public ProgressBar progressbar;
    public Context context;

    public FetchLocationSharingParams(String userId, Context context, ProgressBar progressbar){
        this.userId = userId;
        this.progressbar = progressbar;
        this.context = context;
    }
}
