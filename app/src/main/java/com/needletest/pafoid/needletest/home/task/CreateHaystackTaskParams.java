package com.needletest.pafoid.needletest.home.task;

import android.content.Context;

import com.needletest.pafoid.needletest.models.Haystack;

public class CreateHaystackTaskParams {
    public Context context;
    public Haystack haystack;

    public CreateHaystackTaskParams(Context context, Haystack haystack){
        this.context = context;
        this.haystack = haystack;
    }
}
