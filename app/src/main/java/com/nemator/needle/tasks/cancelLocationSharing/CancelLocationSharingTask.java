package com.nemator.needle.tasks.cancelLocationSharing;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.R;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingResult;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTaskParams;
import com.nemator.needle.tasks.removePostLocationRequest.RemovePostLocationRequestTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CancelLocationSharingTask extends AsyncTask<Void, Void, CancelLocationSharingTaskResult> {
    private static final String CANCEL_LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"cancelLocationSharing.php";
    private static final String TAG = "CreateLocationSharing";

    private CancelLocationSharingResponseHandler delegate;

    private CancelLocationSharingTaskParams params;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public CancelLocationSharingTask(CancelLocationSharingTaskParams params, CancelLocationSharingResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new ProgressDialog(params.context);
        dialog.setMessage(params.context.getResources().getString(R.string.creating_location_sharing));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected CancelLocationSharingTaskResult doInBackground(Void... args) {
        CancelLocationSharingTaskResult result = new CancelLocationSharingTaskResult();
        result.data = params.data;

        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", String.valueOf(params.data.getSenderId())));
            requestParams.add(new BasicNameValuePair("locationSharingId", String.valueOf(params.data.getId())));

            Log.d(TAG, "Cancelling Location Sharing ...");
            JSONObject json = jsonParser.makeHttpRequest(CANCEL_LOCATION_SHARING_URL, "GET", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                result.message = json.getString(AppConstants.TAG_MESSAGE);

                Log.d(TAG, "Location Sharing Cancelled ! " + json.getString(AppConstants.TAG_MESSAGE));

                //Remove from db
                AddPostLocationRequestParams removeParams = new AddPostLocationRequestParams(params.context, LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_LOCATION_SHARING, params.data.getTimeLimit(), params.data.getSenderId(), String.valueOf(params.data.getId()));
                new RemovePostLocationRequestTask(removeParams).execute();

                return result;
            }else{
                Log.d(TAG, "Location Sharing Could Not Be Cancelled! " + json.getString(AppConstants.TAG_MESSAGE));

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void onPostExecute(CancelLocationSharingTaskResult result) {
        super.onPostExecute(result);
        dialog.dismiss();

        delegate.onLocationSharingCancelled(result);
    }

    public interface CancelLocationSharingResponseHandler {
        void onLocationSharingCancelled(CancelLocationSharingTaskResult result);
    }
}
