package com.nemator.needle.home.task.createHaystack;

import android.content.Context;

import com.nemator.needle.models.Haystack;

public class CreateHaystackTaskParams {
    public Context context;
    public Haystack haystack;

    public CreateHaystackTaskParams(Context context, Haystack haystack){
        this.context = context;
        this.haystack = haystack;
    }
}
