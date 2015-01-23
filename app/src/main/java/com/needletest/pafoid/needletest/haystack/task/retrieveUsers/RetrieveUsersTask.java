package com.needletest.pafoid.needletest.haystack.task.retrieveUsers;

import android.os.AsyncTask;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.models.User;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RetrieveUsersTask extends AsyncTask<Void, Void, RetrieveUsersResult> {
    private static final String RETRIEVE_USERS_URL = AppConstants.PROJECT_URL + "retrieveAllUsers.php";
    private static final String TAG = "RetrieveUsersTask";

    private RetrieveUsersParams params;
    private JSONParser jParser = new JSONParser();

    public RetrieveUsersTask(RetrieveUsersParams params){
        this.params = params;
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

            JSONObject json = jParser.makeHttpRequest(RETRIEVE_USERS_URL, "POST", requestParams);
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
                Log.d(TAG, "RetrieveUsersTask failed : "+json.getString(AppConstants.TAG_MESSAGE));
                return result;
            }
        } catch (JSONException e) {
            Log.e(TAG,"JSON Error");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(RetrieveUsersResult result) {
        super.onPostExecute(result);
    }
}
