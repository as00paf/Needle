package com.needletest.pafoid.needletest.haystack.task.addUsers;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.authentication.task.AuthenticationResult;
import com.needletest.pafoid.needletest.haystack.task.activate.ActivateUserParams;
import com.needletest.pafoid.needletest.models.TaskResult;
import com.needletest.pafoid.needletest.models.User;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddUsersTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String ADD_USERS_URL = AppConstants.PROJECT_URL + "addUsers.php";
    private static final String TAG = "AddUsersTask";

    private AddUserResponseHandler delegate;
    private JSONParser jsonParser = new JSONParser();
    private AddUsersTaskParams params;
    private ProgressDialog dialog;

    public AddUsersTask(AddUsersTaskParams params, AddUserResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(params.context);
        dialog.setMessage(params.context.getResources().getString(R.string.adding_users));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        int success;

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, params.haystackId));

            int i;
            for(i=0;i<params.users.size();i++){
                User user = params.users.get(i);
                requestParams.add(new BasicNameValuePair("users[]", String.valueOf(user.getUserId())));
            }

            Log.d(TAG, "Adding Users...");
            JSONObject json = jsonParser.makeHttpRequest(ADD_USERS_URL, "POST", requestParams);

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
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(TaskResult result) {
        dialog.dismiss();
        delegate.onUsersAdded(result);
    }

    public interface AddUserResponseHandler {
        void onUsersAdded(TaskResult result);
    }
}