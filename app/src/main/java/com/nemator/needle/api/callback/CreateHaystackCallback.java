package com.nemator.needle.api.callback;

import android.util.Log;

import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.controller.HaystackController;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateHaystackCallback implements Callback<HaystackResult> {

    private static final String TAG = "CreateHaystackCallback";

    private final HaystackController.CreateHaystackDelegate delegate;

    public CreateHaystackCallback(HaystackController.CreateHaystackDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResponse(Call<HaystackResult> call, Response<HaystackResult> response) {
        HaystackResult result = response.body();
        if(result.getSuccessCode() == 1){
            Log.d(TAG, "Success ! " + result.getMessage());
            delegate.onHaystackCreationSuccess(result.getHaystack());
        }else{
            String msg = "An error occured while creating Haystack. Error : " + result.getMessage();
            Log.d(TAG, msg);

            delegate.onHaystackCreationFailed(msg);
        }
    }

    @Override
    public void onFailure(Call<HaystackResult> call, Throwable t) {
        String msg = "An error occured while creating Haystack : " + t.getMessage();
        Log.d(TAG, msg);

        delegate.onHaystackCreationFailed(msg);
    }
}
