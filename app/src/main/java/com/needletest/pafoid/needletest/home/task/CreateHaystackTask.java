package com.needletest.pafoid.needletest.home.task;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.MapsActivity;
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
            ArrayList<String> haystackUsers = params.haystack.getUsers();
            for(i=0;i<haystackUsers.size();i++){
                String user = haystackUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_user", user));
            }

            ArrayList<String> haystackActiveUsers = params.haystack.getActiveUsers();
            for(i=0;i<haystackActiveUsers.size();i++){
                String user = haystackActiveUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_active_user", user));
            }

            ArrayList<String> haystackBannedUsers = params.haystack.getBannedUsers();
            for(i=0;i<haystackBannedUsers.size();i++){
                String user = haystackBannedUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_banned_user", user));
            }

            Log.d(TAG, "Creating Haystack ...");
            JSONObject json = jsonParser.makeHttpRequest(CREATE_HAYSTACK_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                params.haystack.setId(json.getInt(AppConstants.TAG_HAYSTACK_ID));

                Intent intent = new Intent(params.context, MapsActivity.class);
                intent.putExtra("haystack", (Parcelable) params.haystack);
                params.context.startActivity(intent);

                result.haystack = params.haystack;
                result.message = json.getString(AppConstants.TAG_MESSAGE);;
                return result;
            }else{
                Log.d("CreateHaystack Failure!", json.getString(AppConstants.TAG_MESSAGE));

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
