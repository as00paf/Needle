package com.needletest.pafoid.needletest.home.task;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.HaystackActivity;
import com.needletest.pafoid.needletest.models.User;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateHaystackTask extends AsyncTask<Void, Void, CreateHaystackResult> {
    private static final String CREATE_HAYSTACK_URL = AppConstants.PROJECT_URL +"createHaystack.php";
    private static final String TAG = "CreateHaystackTask";

    private CreateHaystackTaskParams params;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public CreateHaystackTask(CreateHaystackTaskParams params){
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(params.context);
        dialog.setMessage(params.context.getResources().getString(R.string.creatingHaystack));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected CreateHaystackResult doInBackground(Void... args) {
        CreateHaystackResult result = new CreateHaystackResult();
        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("name", params.haystack.getName()));
            requestParams.add(new BasicNameValuePair("owner", String.valueOf(params.haystack.getOwner())));
            requestParams.add(new BasicNameValuePair("isPublic", (params.haystack.getIsPublic()) ? "1" : "0"));
            requestParams.add(new BasicNameValuePair("timeLimit", params.haystack.getTimeLimit()));
            requestParams.add(new BasicNameValuePair("zone", params.haystack.getZone()));
            requestParams.add(new BasicNameValuePair("pictureURL", params.haystack.getPictureURL()));

            int i;
            ArrayList<User> haystackUsers = params.haystack.getUsers();
            for(i=0;i<haystackUsers.size();i++){
                User user = haystackUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_user[]", String.valueOf(user.getUserId())));
            }

            ArrayList<User> haystackActiveUsers = params.haystack.getActiveUsers();
            for(i=0;i<haystackActiveUsers.size();i++){
                User user = haystackActiveUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_active_user[]", String.valueOf(user.getUserId())));
            }

            ArrayList<User> haystackBannedUsers = params.haystack.getBannedUsers();
            for(i=0;i<haystackBannedUsers.size();i++){
                User user = haystackBannedUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_banned_user[]", String.valueOf(user.getUserId())));
            }

            Log.d(TAG, "Creating Haystack ...");
            JSONObject json = jsonParser.makeHttpRequest(CREATE_HAYSTACK_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                params.haystack.setId(json.getInt(AppConstants.TAG_HAYSTACK_ID));
                params.haystack.setUsers(haystackUsers);
                params.haystack.setActiveUsers(haystackActiveUsers);
                params.haystack.setBannedUsers(haystackBannedUsers);

                result.haystack = params.haystack;
                result.message = json.getString(AppConstants.TAG_MESSAGE);;

                Log.d(TAG, "Haystack Created Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));
                return result;
            }else{
                Log.d(TAG, "CreateHaystack Failure! " + json.getString(AppConstants.TAG_MESSAGE));

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    protected void onPostExecute(CreateHaystackResult result) {
        super.onPostExecute(result);
        dialog.dismiss();
    }

}
