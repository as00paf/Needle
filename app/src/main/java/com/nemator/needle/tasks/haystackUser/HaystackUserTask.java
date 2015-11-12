package com.nemator.needle.tasks.haystackUser;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HaystackUserTask extends AsyncTask<Void, Void, HaystackUserTaskResult> {

    private static final String HAYSTACK_USER_URL = AppConstants.PROJECT_URL +"haystackUser.php";
    private static final String TAG = "HaystackUserTask";

    private Object delegate;

    private HaystackUserTaskParams params;
    private ProgressDialog dialog;

    public HaystackUserTask(HaystackUserTaskParams params, Object delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        String message = null;
        Boolean showDialog = false;
        Boolean isCancellable = true;
        switch(params.type){
            case HaystackUserTaskParams.TYPE_GET :
                break;
            case HaystackUserTaskParams.TYPE_ADD:
                message = params.context.getResources().getString(R.string.adding_users);
                isCancellable = false;
                break;
            case HaystackUserTaskParams.TYPE_ACTIVATION:
                break;
            case HaystackUserTaskParams.TYPE_CANCEL :
                message = params.context.getResources().getString(R.string.cancelling_location_sharing);
                break;
            default:
                message = "Haystack User";
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
    protected void onPostExecute(HaystackUserTaskResult result) {
        switch(params.type){
            case HaystackUserTaskParams.TYPE_GET :
                ((GetHaystackUsersResponseHandler) delegate).onUsersRetrieved(result);
                break;
            case HaystackUserTaskParams.TYPE_ADD:
                ((AddHaystackUsersResponseHandler) delegate).onUsersAdded(result);
                break;
            case HaystackUserTaskParams.TYPE_ACTIVATION:
                ((HaystackUserActivationTaskHandler) delegate).onUserActivationToggled(result);
                break;
            case HaystackUserTaskParams.TYPE_CANCEL :
                ((CancelLocationSharingResponseHandler) delegate).onLocationSharingCancelled(result);
                break;
        }

        if(dialog !=null){
            dialog.dismiss();
        }
    }

    @Override
    protected HaystackUserTaskResult doInBackground(Void... args) {
        Log.i(TAG, "Sending Haystack User request of type " + params.type);

        HaystackUserTaskResult result = new HaystackUserTaskResult();
        int success;
        String message = "not set";

       /*try {
            //Request
            JSONObject json;
            if(params.type == HaystackUserTaskParams.TYPE_GET || params.type == HaystackUserTaskParams.TYPE_ADD || params.type == HaystackUserTaskParams.TYPE_CANCEL){
                 //Params
                 List<NameValuePair> requestParams = (List<NameValuePair>) getRequestParams();
                 json = jsonParser.makeHttpRequest(HAYSTACK_USER_URL, params.type, requestParams);
            }else{
                JSONObject jsonObject = (JSONObject) getRequestParams();
                json = jsonParser.makeHttpRequest(HAYSTACK_USER_URL, params.type, null, jsonObject);
            }

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.successMessage = message;

            if(result.successCode == 1){
                switch(params.type){
                    case HaystackUserTaskParams.TYPE_GET:
                        return getHaystackUsers(json, result);
                    case HaystackUserTaskParams.TYPE_ADD:
                        return addUsers(json, result);
                    case HaystackUserTaskParams.TYPE_ACTIVATION:
                        return toggleActivation(json, result);
                    case HaystackUserTaskParams.TYPE_CANCEL:
                        return getCancelledLocationSharing(json, result);

                }
            }else{
                result.isActive = !params.isActive;
                Log.i(TAG, "error");
            }

            return result;
        } catch (Exception e) {
            String msg = "HaystackUserTask with type " + params.type + " Failure ! message :" + e.getMessage();
            Log.d(TAG, msg);
            e.printStackTrace();
            result.isActive = !params.isActive;
            result.successMessage = msg;
        }*/

        return result;
    }

    private Object getRequestParams() {
      /*  List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        JSONObject jsonObject = new JSONObject();
        switch (params.type){
            case LocationSharingParams.TYPE_GET :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_USER_ID, params.userId));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, params.haystackId));
                return requestParams;
            case LocationSharingParams.TYPE_CREATE :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, params.haystackId));

                for(UserVO user : params.users){
                    requestParams.add(new BasicNameValuePair("users[]", String.valueOf(user.getUserId())));
                }

                return requestParams;
            case LocationSharingParams.TYPE_UPDATE :
                try{
                    jsonObject.put(AppConstants.TAG_USER_ID, String.valueOf(params.userId));
                    jsonObject.put(AppConstants.TAG_HAYSTACK_ID, String.valueOf(params.haystackId));
                    jsonObject.put(AppConstants.TAG_IS_ACTIVE, String.valueOf(params.isActive));
                }catch (Exception e){
                    Log.i(TAG, "Exception : "+e.getMessage());
                }

                return jsonObject;
            case LocationSharingParams.TYPE_CANCEL :


                return requestParams;
        }
*/
        return null;
    }

    private HaystackUserTaskResult getHaystackUsers(JSONObject json, HaystackUserTaskResult result){
        try{
            Log.d(TAG, "getHaystackUsers Successful!\n" + json.toString());

            JSONArray users = json.getJSONArray(AppConstants.TAG_USERS);

            for (int i = 0; i < users.length(); i++) {
                JSONObject c = users.getJSONObject(i);

                int id = c.getInt(AppConstants.TAG_ID);
                String name = c.getString("username");
                String gcmRegId = c.getString("gcmRegId");

                UserVO user = new UserVO();
                user.setUserName(name);
                user.setUserId(id);
                user.setGcmRegId(gcmRegId);

                result.users.add(user);
            }

        } catch (Exception e) {
            Log.d(TAG, "getHaystackUsers Failure ! message :" + result.successMessage);
            e.printStackTrace();
        }

        return result;
    }

    private HaystackUserTaskResult addUsers(JSONObject json, HaystackUserTaskResult result){
        try{
            Log.d(TAG, json.toString());
            result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Users could not be added to Haystack. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        Log.d(TAG, "Users added successfuly to Haystack! " + result.successMessage);

        return result;
    }

    private HaystackUserTaskResult toggleActivation(JSONObject json, HaystackUserTaskResult result) {
        try {
            Log.d(TAG, json.toString());

            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                result.isActive = params.isActive;
                return result;
            }else{
                result.successMessage = "LocationSharing Update Failed! " + result.successMessage;
                result.isActive = !params.isActive;
            }
        }catch (JSONException e) {
            e.printStackTrace();
            result.isActive = !params.isActive;
            result.successMessage = "User Activation could not be toggled. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    private HaystackUserTaskResult getCancelledLocationSharing(JSONObject json, HaystackUserTaskResult result) {
        try {
            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);

                Log.d(TAG, "Location Sharing Cancelled ! " + json.getString(AppConstants.TAG_MESSAGE));

                return result;
            }else{
                Log.d(TAG, "Location Sharing Could Not Be Cancelled! " + json.getString(AppConstants.TAG_MESSAGE));

                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Location Sharing could not be cancelled. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    public interface GetHaystackUsersResponseHandler {
        void onUsersRetrieved(HaystackUserTaskResult result);
    }

    public interface AddHaystackUsersResponseHandler {
        void onUsersAdded(HaystackUserTaskResult result);
    }

    public interface HaystackUserActivationTaskHandler{
        void onUserActivationToggled(HaystackUserTaskResult result);
    }

    public interface CancelLocationSharingResponseHandler {
        void onLocationSharingCancelled(HaystackUserTaskResult result);
    }
}
