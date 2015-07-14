package com.nemator.needle.tasks.register;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterTask extends AsyncTask<Void, Void, AuthenticationResult> {

    private static final String LOGIN_URL = AppConstants.PROJECT_URL + "register.php";
    private static final String TAG = "RegisterTask";

    private RegisterResponseHandler delegate;

    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;
    private RegisterTaskParams params;

    public RegisterTask(RegisterTaskParams params, RegisterResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
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
            requestParams.add(new BasicNameValuePair("regId", params.gcmRegId));

            Log.d(TAG, "Registering user ...");
            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", requestParams);
            if(json == null){
                Log.d("Login Failure!", "Timeout Error");
                result.successCode = 0;
                result.message = "An Error Occured. Please try again";
                return result;
            }

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            if (success == 1) {
                Log.d("User Created!", json.toString());

                result.message = json.getString(AppConstants.TAG_MESSAGE);

                int userId = json.getInt(AppConstants.TAG_USER_ID);
                result.user = new UserVO(userId, params.userName, "", params.gcmRegId);
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
        delegate.onRegistrationComplete(result, params.password, true);
    }

    public interface RegisterResponseHandler {
        void onRegistrationComplete(AuthenticationResult result, String password, Boolean rememberMe);
    }
}
