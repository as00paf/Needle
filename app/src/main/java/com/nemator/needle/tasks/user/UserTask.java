package com.nemator.needle.tasks.user;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.haystack.HaystackTaskParams;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.tasks.locationSharing.LocationSharingParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserTask extends AsyncTask<Void, Void, UserTaskResult> {

    private static final String USER_URL = AppConstants.PROJECT_URL +"user.php";
    private static final String TAG = "UserTask";

    private Object delegate;

    private JSONParser jsonParser = new JSONParser();
    private UserTaskParams params;
    private ProgressDialog dialog;

    public UserTask(UserTaskParams params, Object delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        String message = null;
        Boolean showDialog = false;
        Boolean isCancellable = true;
        switch(params.type){
            case UserTaskParams.TYPE_GET :
                break;
            case UserTaskParams.TYPE_REGISTER :
                message = params.context.getResources().getString(R.string.registering_message);
                isCancellable = false;
                showDialog = true;
                break;
            case UserTaskParams.TYPE_UPDATE_GCM_ID :

                break;
            case UserTaskParams.TYPE_UNREGISTER :
                message = params.context.getResources().getString(R.string.registering_message);
                isCancellable = false;
                showDialog = true;
                break;
            default:
                message = "User";
        }

        if(showDialog && message != null){
            dialog = new ProgressDialog(params.context);
            dialog.setMessage(message);
            dialog.setIndeterminate(false);
            dialog.setCancelable(isCancellable);
            dialog.show();
        }


        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(UserTaskResult result) {
        switch(params.type){
            case UserTaskParams.TYPE_GET :

                break;
            case UserTaskParams.TYPE_REGISTER :
                ((RegisterResponseHandler) delegate).onUserRegistered(result);
                break;
            case UserTaskParams.TYPE_UPDATE_GCM_ID :
                ((UpdateGCMIDResponseHandler) delegate).onGCMIDUpdated(result);
                break;
            case UserTaskParams.TYPE_UNREGISTER :
                if(result.successCode==1){
                    Log.d(TAG, "GCMRegId update success");
                }else{
                    Log.d(TAG, "GCMRegId update failure");
                }
                break;
        }

        if(dialog !=null){
            dialog.dismiss();
        }
    }

    @Override
    protected UserTaskResult doInBackground(Void... args) {
        Log.i(TAG, "Sending User request of type " + params.type);

        UserTaskResult result = new UserTaskResult();
        int success;
        String message = "not set";

        try {
            //Request
            JSONObject json;
            if(params.type == UserTaskParams.TYPE_GET || params.type == UserTaskParams.TYPE_REGISTER || params.type == UserTaskParams.TYPE_UNREGISTER){
                 //Params
                 List<NameValuePair> requestParams = (List<NameValuePair>) getRequestParams();
                 json = jsonParser.makeHttpRequest(USER_URL, params.type, requestParams);
            }else{
                JSONObject jsonObject = (JSONObject) getRequestParams();
                json = jsonParser.makeHttpRequest(USER_URL, params.type, null, jsonObject);
            }

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.message = message;

            if(result.successCode == 1){
                Log.i(TAG, "Request of type " + params.type + " succeeded with result : " + json.toString());
                switch(params.type){
                    case UserTaskParams.TYPE_GET:
                        return result;
                    case UserTaskParams.TYPE_REGISTER:
                        return getRegisteredUser(json, result);
                    case UserTaskParams.TYPE_UPDATE_GCM_ID:
                        return result;
                    case UserTaskParams.TYPE_UNREGISTER:
                        return result;

                }
            }else{
                Log.i(TAG, "error");
                result.message = "CreateHaystack Failure! " + result.message;
                Log.d(TAG, result.message);
            }
        } catch (Exception e) {
            result.message = "CreateHaystack Failure! " + e.getMessage();
            Log.d(TAG, result.message);

            e.printStackTrace();
        }

        return result;
    }

    private Object getRequestParams() {
        List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        JSONObject jsonObject = new JSONObject();
        switch (params.type){
            case UserTaskParams.TYPE_GET :
                return null;
            case UserTaskParams.TYPE_REGISTER :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_USER_NAME, params.vo.getUserName()));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_GCM_REG_ID, params.vo.getGcmRegId()));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_LOGIN_TYPE, String.valueOf(params.vo.getLoginType())));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_PICTURE_URL, params.vo.getPictureURL()));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_COVER_PICTURE_URL, params.vo.getCoverPictureURL()));

                switch (params.vo.getLoginType()){
                    case AuthenticationController.LOGIN_TYPE_DEFAULT:
                        requestParams.add(new BasicNameValuePair(AppConstants.TAG_PASSWORD, params.vo.getPassword()));
                        break;
                    case AuthenticationController.LOGIN_TYPE_FACEBOOK:
                        requestParams.add(new BasicNameValuePair(AppConstants.TAG_FB_ID, params.vo.getFbId()));
                        break;
                    case AuthenticationController.LOGIN_TYPE_TWITTER:
                        requestParams.add(new BasicNameValuePair(AppConstants.TAG_TWITTER_ID, params.vo.getTwitterId()));
                        break;
                    case AuthenticationController.LOGIN_TYPE_GOOGLE:
                        requestParams.add(new BasicNameValuePair(AppConstants.TAG_GOOGLE_ID, params.vo.getGoogleId()));
                        break;
                }

                return requestParams;
            case UserTaskParams.TYPE_UPDATE_GCM_ID :
                try{
                    jsonObject.put(AppConstants.TAG_GCM_REG_ID, String.valueOf(params.vo.getGcmRegId()));
                    jsonObject.put(AppConstants.TAG_USER_ID, String.valueOf(params.vo.getUserId()));
                }catch (Exception e){
                    Log.i(TAG, "Exception : "+e.getMessage());
                }

                return jsonObject;
            case UserTaskParams.TYPE_UNREGISTER :
               return null;
        }

        return null;
    }

    private UserTaskResult getRegisteredUser(JSONObject json, UserTaskResult result){
        try{
            result.user = params.vo;
            int userId = json.getInt(AppConstants.TAG_USER_ID);
            int loginType = json.getInt(AppConstants.TAG_LOGIN_TYPE);
            result.user.setUserId(userId);
            result.user.setLoginType(loginType);
            result.user.setPictureURL(params.vo.getPictureURL());
            result.user.setCoverPictureURL(params.vo.getCoverPictureURL());

            switch (loginType){
                case AuthenticationController.LOGIN_TYPE_FACEBOOK:
                    result.user.setFbId(params.vo.getFbId());
                    break;
                case AuthenticationController.LOGIN_TYPE_TWITTER:
                    result.user.setFbId(params.vo.getTwitterId());
                    break;
                case AuthenticationController.LOGIN_TYPE_GOOGLE:
                    result.user.setFbId(params.vo.getGoogleId());
                    break;
            }

            Log.d(TAG, "User Registered Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));
        }catch (JSONException e) {
            e.printStackTrace();
            result.message = "User could not be registered. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    //Interfaces
    public interface RegisterResponseHandler {
        void onUserRegistered(UserTaskResult result);
    }

    public interface UpdateGCMIDResponseHandler {
        void onGCMIDUpdated(UserTaskResult result);
    }
}
