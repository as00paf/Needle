package com.nemator.needle.tasks.createHaystack;

import android.content.Context;

import com.nemator.needle.models.vo.HaystackVO;

public class CreateHaystackTaskParams {
    public Context context;
    public HaystackVO haystack;

    public CreateHaystackTaskParams(Context context, HaystackVO haystack){
        this.context = context;
        this.haystack = haystack;
    }
}
