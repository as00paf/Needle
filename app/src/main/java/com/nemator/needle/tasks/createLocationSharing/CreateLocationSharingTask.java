package com.nemator.needle.tasks.createLocationSharing;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.createHaystack.CreateHaystackResult;
import com.nemator.needle.tasks.createHaystack.CreateHaystackTaskParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateLocationSharingTask extends AsyncTask<Void, Void, CreateLocationSharingResult> {
    private static final String CREATE_LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"shareLocation.php";
    private static final String TAG = "CreateLocationSharing";
    private static final String SENDER_ID = "648034739265";

    private CreateLocationSharingResponseHandler delegate;

    private CreateLocationSharingTaskParams params;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public CreateLocationSharingTask(CreateLocationSharingTaskParams params, CreateLocationSharingResponseHandler delegate){
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
    protected CreateLocationSharingResult doInBackground(Void... args) {
        CreateLocationSharingResult result = new CreateLocationSharingResult();
        result.params = this.params;

        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("senderId", String.valueOf(params.locationSharing.getSenderId())));
            requestParams.add(new BasicNameValuePair("senderName", params.locationSharing.getSenderName()));
            requestParams.add(new BasicNameValuePair("receiverId", String.valueOf(params.locationSharing.getReceiverId())));
            requestParams.add(new BasicNameValuePair("receiverName", params.locationSharing.getReceiverName()));
            requestParams.add(new BasicNameValuePair("timeLimit", params.locationSharing.getTimeLimit()));

            //Send Notification Params
            String notificationMessage = params.locationSharing.getSenderName() + " shared his location with you !";
            requestParams.add(new BasicNameValuePair("notificationMessage", notificationMessage));
            requestParams.add(new BasicNameValuePair("action", "com.nemator.needle.gcm"));
            requestParams.add(new BasicNameValuePair("notificationType", "LocationSharing"));
            requestParams.add(new BasicNameValuePair("regId", params.gcmRegId));

            Log.d(TAG, "Creating Location Sharing ...");
            JSONObject json = jsonParser.makeHttpRequest(CREATE_LOCATION_SHARING_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                params.locationSharing.setId(json.getInt(AppConstants.TAG_LOCATION_SHARING_ID));

                result.locationSharing = params.locationSharing;
                result.message = json.getString(AppConstants.TAG_MESSAGE);



                Log.d(TAG, "Location Sharing Created Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));

                return result;
            }else{
                Log.d(TAG, "Location Sharing Failure! " + json.getString(AppConstants.TAG_MESSAGE));

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void onPostExecute(CreateLocationSharingResult result) {
        super.onPostExecute(result);
        dialog.dismiss();

        delegate.onLocationSharingCreated(result);
    }

    public interface CreateLocationSharingResponseHandler {
        void onLocationSharingCreated(CreateLocationSharingResult result);
    }
}
