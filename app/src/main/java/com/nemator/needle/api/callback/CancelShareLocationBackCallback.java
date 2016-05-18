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

public class CancelShareLocationBackCallback implements Callback<NeedleResult> {
    public static String TAG = "CancelShareLocationBackCallback";

    private final NeedleController.CancelShareBackDelegate delegate;

    public CancelShareLocationBackCallback(NeedleController.CancelShareBackDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResponse(Call<NeedleResult> call, Response<NeedleResult> response) {
        NeedleResult result = response.body();
        if(result.getSuccessCode() == 1){
            Log.d(TAG, "Cancelled location share back !");

            NeedleVO vo = result.getLocationSharing();

            //Remove Post Location Request to the DB
            Needle.serviceController.getService()
                    .removePostLocationRequest(PostLocationRequest.Type.NEEDLE_BACK, vo.getTimeLimit(), vo.getSender().getId(), String.valueOf(vo.getId()));

            delegate.onShareBackCancelSuccess(vo);
        }else{
            String msg = "Failed to cancel location share back !";
            Log.d(TAG, msg);
            delegate.onShareBackCancelFailed(msg);
        }
    }

    @Override
    public void onFailure(Call<NeedleResult> call, Throwable t) {
        String msg = "Cancel share back failed. Error : " + t.getMessage();
        Log.d(TAG, msg);
        delegate.onShareBackCancelFailed(msg);
    }
}
