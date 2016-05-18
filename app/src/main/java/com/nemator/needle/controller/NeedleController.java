package com.nemator.needle.controller;

import android.util.Log;

import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.callback.CancelLocationSharingCallback;
import com.nemator.needle.api.callback.CancelShareLocationBackCallback;
import com.nemator.needle.api.callback.CreateNeedleCallback;
import com.nemator.needle.api.callback.ShareLocationBackCallback;
import com.nemator.needle.models.vo.NeedleVO;

public class NeedleController {

    private static final String TAG = "LocationSharingCtlr";

    public static void createNeedle(NeedleVO locationSharing, CreateNeedleDelegate delegate){
        ApiClient.getInstance().createNeedle(locationSharing, new CreateNeedleCallback(delegate));
    }

    public static void shareLocationBack(NeedleVO locationSharing, ShareLocationBackDelegate delegate){
        Log.i(TAG, "Trying to share location back");
        NeedleVO vo = locationSharing.clone();
        vo.setShareBack(true);
        ApiClient.getInstance().shareLocationBack(vo, new ShareLocationBackCallback(delegate));
    }

    public static void cancelShareBack(NeedleVO locationSharing, CancelShareBackDelegate delegate){
        Log.i(TAG, "Trying to cancel share back");
        NeedleVO vo = locationSharing.clone();
        vo.setShareBack(false);
        ApiClient.getInstance().shareLocationBack(vo, new CancelShareLocationBackCallback(delegate));
    }

    public static  void cancelNeedle(NeedleVO locationSharing, CancelNeedleDelegate delegate){
        Log.i(TAG, "Trying to cancel location sharing");
        ApiClient.getInstance().cancelNeedle(locationSharing, new CancelLocationSharingCallback(delegate, locationSharing));
    }

    //Interfaces
    public interface CreateNeedleDelegate {
        void onNeedleCreationSuccess(NeedleVO locationSharing);
        void onNeedleCreationFailed(String result);
    }

    public interface CancelNeedleDelegate {
        void onNeedleCancelSuccess(NeedleVO locationSharing);
        void onNeedleCancelFailed(String result);
    }

    public interface ShareLocationBackDelegate {
        void onLocationShareBackSuccess(NeedleVO locationSharing);
        void onLocationShareBackFailed(String result);
    }

    public interface CancelShareBackDelegate {
        void onShareBackCancelSuccess(NeedleVO locationSharing);
        void onShareBackCancelFailed(String result);
    }
}
