package com.needletest.pafoid.needletest.haystack.task;

import android.content.Context;
import android.location.Location;

public class PostLocationParams{
    public String username;
    public Location location;
    public Context context;

    public PostLocationParams(Context context, String username, Location location){
        this.context = context;
        this.username = username;
        this.location = location;
    }
}