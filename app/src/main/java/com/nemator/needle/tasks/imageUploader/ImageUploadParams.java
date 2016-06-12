package com.nemator.needle.tasks.imageUploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ProgressBar;

public class ImageUploadParams {
    public String fileName;
    public Bitmap bitmap;
    public ProgressBar progressBar;

    public ImageUploadParams(Bitmap bitmap, String fileName){
        this.bitmap = bitmap;
        this.fileName = fileName;
    }

    public ImageUploadParams(Bitmap bitmap, ProgressBar progressbar){
        this.bitmap = bitmap;
        this.progressBar = progressbar;
    }
}
