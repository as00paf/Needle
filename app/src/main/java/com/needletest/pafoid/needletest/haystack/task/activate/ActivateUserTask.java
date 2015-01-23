package com.needletest.pafoid.needletest.haystack.task.activate;

import android.os.AsyncTask;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.authentication.task.AuthenticationResult;
import com.needletest.pafoid.needletest.models.TaskResult;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivateUserTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String ACTIVATE_USER_URL = AppConstants.PROJECT_URL + "activateUser.php";
    private static final String TAG = "ActivateUserTask";

    private ActivateUserResponseHandler delegate;
    private JSONParser jsonParser = new JSONParser();
    private ActivateUserParams params;

    public ActivateUserTask(ActivateUserParams params, ActivateUserResponseHandler delegate){
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

            Log.d(TAG, "Activating User...");
            JSONObject json = jsonParser.makeHttpRequest(ACTIVATE_USER_URL, "POST", requestParams);

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
            result.message = "Error Activating User";
            return result;
        }
    }

    protected void onPostExecute(TaskResult result) {
        delegate.onUserActivated(result);
    }

    public interface ActivateUserResponseHandler {
        void onUserActivated(TaskResult result);
    }
}