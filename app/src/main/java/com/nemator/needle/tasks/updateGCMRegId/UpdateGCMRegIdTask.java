package com.nemator.needle.tasks.updateGCMRegId;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 14/07/2015.
 */
public class UpdateGCMRegIdTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String UPDATE_GCM_REGID_URL = AppConstants.PROJECT_URL + "updateGCMRegId.php";
    private static final String TAG = "UpdateGCMRegIdTask";

    private JSONParser jParser = new JSONParser();

    private UpdateGCMRegIdTaskParams params;

    private UpdateGCMRegIdTaskHandler delegate;

    public UpdateGCMRegIdTask(UpdateGCMRegIdTaskParams params, UpdateGCMRegIdTaskHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "Updating GCM Reg Id");
        super.onPreExecute();
    }

    @Override
    protected TaskResult doInBackground(Void... param) {
        TaskResult result = new TaskResult();
        int success = 0;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("regId", params.regId));
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            JSONObject json = jParser.makeHttpRequest(UPDATE_GCM_REGID_URL, "GET", requestParams);
            if(json!=null){
                success = json.getInt(AppConstants.TAG_SUCCESS);
                result.successCode = success;
                if (success == 1) {
                    Log.d(TAG, "GCMRegId update success");
                    result.message = json.getString(AppConstants.TAG_MESSAGE);

                    return result;
                }else{
                    Log.d(TAG, "GCMRegId update failed : "+json.getString(AppConstants.TAG_MESSAGE));
                    result.message = json.getString(AppConstants.TAG_MESSAGE);
                    return result;
                }
            }else{
                Log.e(TAG,"GCMRegId update JSON Error");

                result.successCode = 0;
                result.message = "GCMRegId update failed";
            }

        } catch (JSONException e) {
            Log.e(TAG,"GCMRegId update JSON Error");
            //e.printStackTrace();
            result.successCode = 0;
            result.message = "GCMRegId update failed";
            return  result;
        }

        return  result;
    }

    @Override
    protected void onPostExecute(TaskResult taskResult) {
        delegate.onGCMRegIdUpdate(taskResult);
        super.onPostExecute(taskResult);
    }

    public interface UpdateGCMRegIdTaskHandler{
        void onGCMRegIdUpdate(TaskResult result);
    }
}
