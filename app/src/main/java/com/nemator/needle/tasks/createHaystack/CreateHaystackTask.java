package com.nemator.needle.tasks.createHaystack;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.JSONParser;
import com.nemator.needle.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateHaystackTask extends AsyncTask<Void, Void, CreateHaystackResult> {
    private static final String CREATE_HAYSTACK_URL = AppConstants.PROJECT_URL +"createHaystack.php";
    private static final String TAG = "CreateHaystackTask";

    private CreateHaystackResponseHandler delegate;

    private CreateHaystackTaskParams params;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public CreateHaystackTask(CreateHaystackTaskParams params, CreateHaystackResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
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
        result.params = this.params;
        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("name", params.haystack.getName()));
            requestParams.add(new BasicNameValuePair("owner", String.valueOf(params.haystack.getOwner())));
            requestParams.add(new BasicNameValuePair("isPublic", (params.haystack.getIsPublic()) ? "1" : "0"));
            requestParams.add(new BasicNameValuePair("timeLimit", params.haystack.getTimeLimit()));
            requestParams.add(new BasicNameValuePair("zoneRadius", String.valueOf(params.haystack.getZoneRadius())));
            requestParams.add(new BasicNameValuePair("isCircle", (params.haystack.getIsPublic()) ? "1" : "0"));
            requestParams.add(new BasicNameValuePair("lat", String.valueOf(params.haystack.getPosition().latitude)));
            requestParams.add(new BasicNameValuePair("lng", String.valueOf(params.haystack.getPosition().longitude)));
            requestParams.add(new BasicNameValuePair("pictureURL", params.haystack.getPictureURL()));

            int i;
            ArrayList<UserVO> haystackUsers = params.haystack.getUsers();
            for(i=0;i<haystackUsers.size();i++){
                UserVO user = haystackUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_user[]", String.valueOf(user.getUserId())));
            }

            ArrayList<UserVO> haystackActiveUsers = params.haystack.getActiveUsers();
            for(i=0;i<haystackActiveUsers.size();i++){
                UserVO user = haystackActiveUsers.get(i);
                requestParams.add(new BasicNameValuePair("haystack_active_user[]", String.valueOf(user.getUserId())));
            }

            ArrayList<UserVO> haystackBannedUsers = params.haystack.getBannedUsers();
            for(i=0;i<haystackBannedUsers.size();i++){
                UserVO user = haystackBannedUsers.get(i);
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
                result.message = json.getString(AppConstants.TAG_MESSAGE);

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

        delegate.onHaystackCreated(result);
    }

    public interface CreateHaystackResponseHandler {
        void onHaystackCreated(CreateHaystackResult result);
    }
}