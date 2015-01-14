package com.needletest.pafoid.needletest.authentication.task;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.activities.HomeActivity;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginTask extends AsyncTask<Void, Void, AuthenticationResult> {
    private static final String LOGIN_URL = AppConstants.PROJECT_URL +"login.php";
    private static final String TAG = "LoginTask";

    private LoginTaskParams params;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public LoginTask(LoginTaskParams params){
        this.params = params;
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
    protected AuthenticationResult doInBackground(Void... args) {
        AuthenticationResult result = new AuthenticationResult();

        int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", params.userName));
            requestParams.add(new BasicNameValuePair("password", params.password));

            Log.d(TAG, "Attempting Login");
            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                Log.d("Login Successful!", json.toString());

                // Save user data
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(params.context);
                SharedPreferences.Editor edit = sp.edit();

                edit.putString("username", params.userName);
                edit.putInt("userId", json.getInt(AppConstants.TAG_USER_ID));
                edit.putBoolean("rememberMe", params.rememberMe);

                if(params.rememberMe){
                    edit.putString("password", params.password);
                }

                edit.commit();

                //Intent i = new Intent(Login.this, MapsActivity.class);
                Intent i = new Intent(params.context, HomeActivity.class);
                params.context.startActivity(i);

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;
            }else{
                Log.d("Login Failure!", json.getString(AppConstants.TAG_MESSAGE));
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    protected void onPostExecute(AuthenticationResult result) {
        dialog.dismiss();
    }

}