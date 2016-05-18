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

public class CreateNeedleCallback implements Callback<NeedleResult> {

    private static final String TAG = "LocShrngCreateCallback";

    private final NeedleController.CreateNeedleDelegate delegate;

    public CreateNeedleCallback(NeedleController.CreateNeedleDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResponse(Call<NeedleResult> call, Response<NeedleResult> response) {
        NeedleResult result = response.body();

        if(result.getSuccessCode() == 1){
            NeedleVO vo = result.getLocationSharing();

            //Add Post Location Request to the DB
            Needle.serviceController.getService()
                    .addPostLocationRequest(PostLocationRequest.Type.NEEDLE, vo.getTimeLimit(), vo.getSender().getId(), String.valueOf(vo.getId()));

            Needle.serviceController.getService().postLocation();

            delegate.onNeedleCreationSuccess(vo);
        }else{
            String msg = "Location Sharing not created. Error : " + result.getMessage();
            Log.e(TAG, msg);
            delegate.onNeedleCreationFailed(msg);
        }
    }

    @Override
    public void onFailure(Call<NeedleResult> call, Throwable t) {
        String msg = "Location Sharing not created. Error : " + t.getMessage();
        Log.e(TAG, msg);
        delegate.onNeedleCreationFailed(msg);
    }
}
