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

public class CancelLocationSharingCallback implements Callback<NeedleResult> {

    private static final String TAG = "LocShrngCancelCallback";

    private final NeedleController.CancelNeedleDelegate delegate;
    private final NeedleVO locationSharing;

    public CancelLocationSharingCallback(NeedleController.CancelNeedleDelegate delegate, NeedleVO locationSharing) {
        this.delegate = delegate;
        this.locationSharing = locationSharing;
    }

    @Override
    public void onResponse(Call<NeedleResult> call, Response<NeedleResult> response) {
        NeedleResult result = response.body();
        if(result.getSuccessCode() == 1){
            Log.d(TAG, "Location sharing cancelled !");

            //Remove Post Location Request to the DB
            Needle.serviceController.getService()
                    .removePostLocationRequest(PostLocationRequest.Type.NEEDLE, locationSharing.getTimeLimit(), locationSharing.getSender().getId(), String.valueOf(locationSharing.getId()));

            delegate.onNeedleCancelSuccess(result.getLocationSharing());
        }else{
            String msg = "Failed to cancel location sharing !";
            Log.d(TAG, msg);
            delegate.onNeedleCancelFailed(msg);
        }
    }

    @Override
    public void onFailure(Call<NeedleResult> call, Throwable t) {
        String msg = "Cancel Location Sharing failed. Error : " + t.getMessage();
        Log.d(TAG, msg);
        delegate.onNeedleCancelFailed(msg);
    }
}
