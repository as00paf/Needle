package com.nemator.needle.tasks.login;

import android.os.AsyncTask;

import com.nemator.needle.utils.AppConstants;

public class LoginTask extends AsyncTask<Void, Void, LoginTaskResult> {
    private static final String LOGIN_URL = AppConstants.PROJECT_URL +"login.php";
    private static final String TAG = "LoginTask";

    private LoginResponseHandler delegate;

    private LoginTaskParams params;

    public LoginTask(LoginTaskParams params, LoginResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //TODO: replace by localized text
        if(params.textView.get() != null){
            params.textView.get().setText("Attempting login...");
        }
    }

    @Override
    protected LoginTaskResult doInBackground(Void... args) {
        LoginTaskResult result = new LoginTaskResult();
        result.setType(params.user.getLoginType());

        /*int success;
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", params.user.getUserName()));
            requestParams.add(new BasicNameValuePair("regId", params.user.getGcmRegId()));
            requestParams.add(new BasicNameValuePair("type", String.valueOf(params.user.getLoginType())));

            if(params.user.getLoginType() == AuthenticationController.LOGIN_TYPE_DEFAULT){
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_PASSWORD, params.user.getPassword()));
            }else {
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_SOCIAL_NETWORK_USER_ID, params.user.getSocialNetworkUserId()));
            }

            JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            if (success == 1) {
                Log.d("Login Successful!", json.toString());

                // Save user data
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(params.context);
                SharedPreferences.Editor edit = sp.edit();

                edit.putString(AppConstants.TAG_USER_NAME, params.user.getUserName());
                edit.putInt(AppConstants.TAG_USER_ID, json.getInt(AppConstants.TAG_USER_ID));
                edit.putInt(AppConstants.TAG_LOGIN_TYPE, params.user.getLoginType());
                edit.putString(AppConstants.TAG_PASSWORD, params.user.getPassword());

                edit.commit();

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                int userId = json.getInt(AppConstants.TAG_USER_ID);
                result.user = new UserVO(userId, params.user.getUserName(), "", params.user.getGcmRegId());
                result.user.setSocialNetworkUserId(params.user.getSocialNetworkUserId());
                result.user.setLoginType(params.user.getLoginType());
                result.user.setPictureURL(json.getString(AppConstants.TAG_PICTURE_URL));
                result.user.setCoverPictureURL(json.getString(AppConstants.TAG_COVER_PICTURE_URL));
                result.locationSharingCount = json.getInt(AppConstants.TAG_LOCATION_SHARING_COUNT);
                result.haystackCount = json.getInt(AppConstants.TAG_HAYSTACK_COUNT);

                return result;
            }else{
                Log.d(TAG, json.getString(AppConstants.TAG_MESSAGE));
                result.message = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        } catch (Exception e) {
            Log.d(TAG, "Error : " + e.getMessage());
            result.successCode = 0;
            result.message = "Login Failure! Error : " + e.getMessage();
            return result;
        }*/

        return null;
    }

    @Override
    protected void onPostExecute(LoginTaskResult result) {
        delegate.onLoginComplete(result);
        super.onPostExecute(result);
    }

    public interface LoginResponseHandler {
        void onLoginComplete(LoginTaskResult result);
    }

}