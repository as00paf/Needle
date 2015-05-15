package com.nemator.needle.tasks.imageUploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ProgressBar;

public class ImageUploadParams {
    public String fileName;
    public Bitmap bitmap;
    public Context context;
    public ProgressBar progressBar;

    public ImageUploadParams(Bitmap bitmap, String fileName, Context context){
        this.bitmap = bitmap;
        this.fileName = fileName;
        this.context = context;
    }

    public ImageUploadParams(Bitmap bitmap, Context context, ProgressBar progressbar){
        this.bitmap = bitmap;
        this.context = context;
        this.progressBar = progressbar;
    }
}
