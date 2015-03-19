package com.nemator.needle.haystack.task.retrieveUsers;

import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.AppConstants;
import com.nemator.needle.models.User;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RetrieveUsersTask extends AsyncTask<Void, Void, RetrieveUsersResult> {
    private static final String RETRIEVE_USERS_URL = AppConstants.PROJECT_URL + "retrieveAllUsers.php";
    private static final String RETRIEVE_NOT_IN_HAYSTACK_USERS_URL = AppConstants.PROJECT_URL + "fetchUsersNotInHaystack.php";
    private static final String RETRIEVE_ACTIVE_USERS_URL = AppConstants.PROJECT_URL + "fetchHaystackActiveUsers.php";

    private static final String TAG = "RetrieveUsersTask";

    private RetrieveUsersResponseHandler delegate;
    private RetrieveUsersParams params;
    private JSONParser jParser = new JSONParser();

    public RetrieveUsersTask(RetrieveUsersParams params, RetrieveUsersResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected RetrieveUsersResult doInBackground(Void... arg) {
        RetrieveUsersResult result = new RetrieveUsersResult();
        int success;

        result.userList = new ArrayList<User>();
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            Log.d(TAG, "Retrieving Users...");
            String url;
            switch (params.type){
                case TYPE_ALL_USERS:
                    url = RETRIEVE_USERS_URL;
                    break;
                case TYPE_USERS_NOT_IN_HAYSTACK:
                    requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, String.valueOf(params.haystackId)));
                    url = RETRIEVE_NOT_IN_HAYSTACK_USERS_URL;
                    break;
                case TYPE_HAYSTACK_ACTIVE_USERS:
                    requestParams.add(new BasicNameValuePair("haystackId", String.valueOf(params.haystackId)));
                    url = RETRIEVE_ACTIVE_USERS_URL;
                    break;
                default :
                    url = RETRIEVE_USERS_URL;
                    break;
            }

            JSONObject json = jParser.makeHttpRequest(url, "POST", requestParams);
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            result.userList = new ArrayList<User>();

            if (success == 1) {
                Log.d(TAG, "RetrieveUsersTask success");

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                result.users = json.getJSONArray(AppConstants.TAG_USERS);

                for (int i = 0; i < result.users.length(); i++) {
                    JSONObject c = result.users.getJSONObject(i);

                    int id = c.getInt(AppConstants.TAG_ID);
                    String name = c.getString("username");

                    User user = new User();
                    user.setUserName(name);
                    user.setUserId(id);

                    result.userList.add(user);
                }
                return result;
            }else{
                Log.d(TAG, "RetrieveUsersTask failed");
                result.successCode = 0;
                result.message = "Error Retrieving Users";
                return result;
            }
        } catch (JSONException e) {
            //Log.e(TAG,"JSON Error");
           // e.printStackTrace();

            result.successCode = 0;
            result.message = "Error Retrieving Users";
            return result;
        }
    }

    @Override
    protected void onPostExecute(RetrieveUsersResult result) {
        super.onPostExecute(result);
        delegate.onUsersRetrieved(result);
    }

    public interface RetrieveUsersResponseHandler {
        void onUsersRetrieved(RetrieveUsersResult result);
    }
}
