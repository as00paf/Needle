package com.needletest.pafoid.needletest.authentication.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterTask extends AsyncTask<Void, Void, AuthenticationResult> {

    private static final String LOGIN_URL = AppConstants.PROJECT_URL + "register.php";
    private static final String TAG = "RegisterTask";

    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;
    private RegisterTaskParams params;

    public RegisterTask(RegisterTaskParams params){
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(params.context);
        dialog.setMessage(params.context.getResources().getString(R.string.registerDialog));
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

            Log.d(TAG, "Registering user ...");
            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            if (success == 1) {
                Log.d("User Created!", json.toString());

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
