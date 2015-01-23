package com.needletest.pafoid.needletest.haystack.task.postLocation;

import android.os.AsyncTask;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.authentication.task.AuthenticationResult;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostLocationTask extends AsyncTask<Void, Void, PostLocationResult> {
    private static final String POST_LOCATION_URL = AppConstants.PROJECT_URL + "updateLocation.php";
    private static final String TAG = "PostLocationTask";

    private JSONParser jsonParser = new JSONParser();
    private PostLocationParams params;

    public PostLocationTask(PostLocationParams params){
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected PostLocationResult doInBackground(Void... args) {
        PostLocationResult result = new PostLocationResult();

        int success;

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", params.username));
            requestParams.add(new BasicNameValuePair("userId", params.userId));
            requestParams.add(new BasicNameValuePair("lat", String.valueOf(params.position.latitude)));
            requestParams.add(new BasicNameValuePair("lng", String.valueOf(params.position.longitude)));

            if(params.verbose) Log.d(TAG, "Posting Location...");
            JSONObject json = jsonParser.makeHttpRequest(POST_LOCATION_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                if(params.verbose) Log.d(TAG, json.toString());
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;
            }else{
                if(params.verbose) Log.e(TAG, json.getString(AppConstants.TAG_MESSAGE));
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
