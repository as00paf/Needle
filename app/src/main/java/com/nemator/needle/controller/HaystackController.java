package com.nemator.needle.controller;

import android.graphics.Bitmap;

import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.callback.CreateHaystackCallback;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.tasks.imageUploader.ImageUploadParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadResult;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;

public class HaystackController {
    private static final String TAG = "HaystackController";

    public static void createHaystack(final HaystackVO haystack, Bitmap image, final CreateHaystackDelegate delegate){
        //File Upload
        if(image != null){
            ImageUploadParams uploadParams = new ImageUploadParams(image, haystack.getName());
            try{
                ImageUploaderTask uploadTask = new ImageUploaderTask(uploadParams, new ImageUploaderTask.ImageUploadResponseHandler(){

                    @Override
                    public void onImageUploaded(ImageUploadResult result) {
                        if(result.successCode == 1) {
                            haystack.setPictureURL(result.imageURL);

                            //Create Haystack
                            ApiClient.getInstance().createHaystack(haystack, new CreateHaystackCallback(delegate));
                        }else{
                            delegate.onHaystackImageUploadFailed();
                        }
                    }
                });
                uploadTask.execute();
            }catch (Exception e) {
                delegate.onHaystackImageUploadFailed();
            }
        }else{
            //Create Haystack
            ApiClient.getInstance().createHaystack(haystack, new CreateHaystackCallback(delegate));
        }
    }

    //Interfaces
    public interface CreateHaystackDelegate {
        void onHaystackCreationSuccess(HaystackVO haystack);
        void onHaystackCreationFailed(String result);
        void onHaystackImageUploadFailed();
    }
}
