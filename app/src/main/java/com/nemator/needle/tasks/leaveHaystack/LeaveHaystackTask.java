package com.nemator.needle.tasks.leaveHaystack;

import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaveHaystackTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String LEAVE_HAYSTACK_URL = AppConstants.PROJECT_URL + "leaveHaystack.php";
    private static final String TAG = "LeaveHaystackTask";

    private LeaveHaystackResponseHandler delegate;
    private JSONParser jsonParser = new JSONParser();
    private LeaveHaystackParams params;

    public LeaveHaystackTask(LeaveHaystackParams params, LeaveHaystackResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        int success;

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair(AppConstants.TAG_USER_ID, params.userId));
            requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, params.haystackId));

            Log.d(TAG, "Leaving Haystack...");
            JSONObject json = jsonParser.makeHttpRequest(LEAVE_HAYSTACK_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                Log.d(TAG, json.toString());
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;
            }else{
                Log.e(TAG, json.getString(AppConstants.TAG_MESSAGE));
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            result.successCode = 0;
            result.message = "Error Leaving Haystack";
            return result;
        }
    }

    protected void onPostExecute(TaskResult result) {
        delegate.onHaystackLeft(result);
    }

    public interface LeaveHaystackResponseHandler {
        void onHaystackLeft(TaskResult result);
    }
}