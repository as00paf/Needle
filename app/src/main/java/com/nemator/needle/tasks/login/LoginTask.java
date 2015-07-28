package com.nemator.needle.tasks.login;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginTask extends AsyncTask<Void, Void, LoginTaskResult> {
    private static final String LOGIN_URL = AppConstants.PROJECT_URL +"login.php";
    private static final String TAG = "LoginTask";

    private LoginResponseHandler delegate;

    private LoginTaskParams params;
    private JSONParser jsonParser = new JSONParser(7000, 10000);
    private ProgressDialog dialog;

    public LoginTask(LoginTaskParams params, LoginResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(params.context);
        dialog.setMessage("Attempting login...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected LoginTaskResult doInBackground(Void... args) {
        LoginTaskResult result = new LoginTaskResult();

        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", params.userName));
            requestParams.add(new BasicNameValuePair("password", params.password));
            requestParams.add(new BasicNameValuePair("regId", params.gcmRegId));

            if(params.verbose) Log.d(TAG, "Attempting Login");
            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                Log.d("Login Successful!", json.toString());

                // Save user data
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(params.context);
                SharedPreferences.Editor edit = sp.edit();

                edit.putString(AppConstants.TAG_USER_NAME, params.userName);
                edit.putInt(AppConstants.TAG_USER_ID, json.getInt(AppConstants.TAG_USER_ID));
                edit.putBoolean("rememberMe", params.rememberMe);

                if(params.rememberMe){
                    edit.putString("password", params.password);
                }

                edit.commit();

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                int userId = json.getInt(AppConstants.TAG_USER_ID);
                result.user = new UserVO(userId, params.userName, "", params.gcmRegId);
                result.locationSharingCount = json.getInt(AppConstants.TAG_LOCATION_SHARING_COUNT);
                result.haystackCount = json.getInt(AppConstants.TAG_HAYSTACK_COUNT);

                return result;
            }else{
                Log.d("Login Failure!", json.getString(AppConstants.TAG_MESSAGE));
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (Exception e) {
            Log.d("Login Failure!", "Error : " + e.getMessage());
            result.successCode = 0;
            result.message = "Login Failure! Error : " + e.getMessage();
            return result;
        }
    }

    @Override
    protected void onPostExecute(LoginTaskResult result) {
        if(dialog!=null) dialog.dismiss();
        delegate.onLoginComplete(result);
        super.onPostExecute(result);
    }

    public interface LoginResponseHandler {
        void onLoginComplete(LoginTaskResult result);
    }

}