package com.nemator.needle.api.callback;

import android.util.Log;

import com.nemator.needle.Needle;
import com.nemator.needle.api.result.NeedleResult;
import com.nemator.needle.controller.NeedleController;
import com.nemator.needle.data.PostLocationRequest;
import com.nemator.needle.models.vo.NeedleVO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareLocationBackCallback implements Callback<NeedleResult> {
    public static String TAG = "ShareLocationBackCallback";

    private final NeedleController.ShareLocationBackDelegate delegate;

    public ShareLocationBackCallback(NeedleController.ShareLocationBackDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResponse(Call<NeedleResult> call, Response<NeedleResult> response) {
        NeedleResult result = response.body();
        if(result.getSuccessCode() == 1){
            Log.d(TAG, "Location shared back !");

            NeedleVO vo = result.getLocationSharing();

            //Add Post Location Request to the DB
            Needle.serviceController.getService()
                    .addPostLocationRequest(PostLocationRequest.Type.NEEDLE_BACK, vo.getTimeLimit(), vo.getSender().getId(), String.valueOf(vo.getId()));

            delegate.onLocationShareBackSuccess(vo);
        }else{
            String msg = "Failed to share location back !";
            Log.d(TAG, msg);
            delegate.onLocationShareBackFailed(msg);
        }
    }

    @Override
    public void onFailure(Call<NeedleResult> call, Throwable t) {
        String msg = "Location Share back failed. Error : " + t.getMessage();
        Log.d(TAG, msg);
        delegate.onLocationShareBackFailed(msg);
    }
}
