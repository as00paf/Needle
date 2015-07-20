package com.nemator.needle.controller;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.updateGCMRegId.UpdateGCMRegIdTask;
import com.nemator.needle.tasks.updateGCMRegId.UpdateGCMRegIdTaskParams;
import com.nemator.needle.utils.AppConstants;

import java.io.IOException;

public class GCMController implements UpdateGCMRegIdTask.UpdateGCMRegIdTaskHandler {

    public static String TAG = "GCMController";

    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private static final String GCM_SENDER_ID = "648034739265";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private MainActivity activity;
    private UserModel userModel;
    private SharedPreferences mSharedPreferences;

    GoogleCloudMessaging gcm;
    int tryCount = 0;

    public GCMController(MainActivity activity, UserModel userModel){
        this.activity = activity;
        this.userModel = userModel;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        init();
    }

    private void init(){
        //GCM
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());

            // Read saved registration id from shared preferences.
            userModel.setGcmRegId(mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, ""));

            if (TextUtils.isEmpty(userModel.getGcmRegId())) {
                handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            }else{
                new RegisterGCMTask().execute();
            }
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onGCMRegIdUpdate(TaskResult result) {
        if(result.successCode != 1){
            tryCount++;
            if(tryCount < 3){
                UpdateGCMRegIdTaskParams params = new UpdateGCMRegIdTaskParams(activity, String.valueOf(userModel.getUserId()), userModel.getGcmRegId());
                new UpdateGCMRegIdTask(params, GCMController.this).execute();
            }else{
                Toast.makeText(activity, "Error While Updating GCM RegID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class RegisterGCMTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Already registered with GCM
            try {
                userModel.setGcmRegId(gcm.register(GCM_SENDER_ID));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }
    }

    private class GCMRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (gcm == null && checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());
            }
            try {
                return userModel.setGcmRegId(gcm.register(activity.getResources().getString(R.string.gcm_defaultSenderId)));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                Log.i(TAG, "Was already registered with GCM " + userModel.getGcmRegId());
            }else{
                Log.i(TAG, "Needs to register with GCM " + userModel.getGcmRegId());

                if(userModel.getUserId() != -1){
                    UpdateGCMRegIdTaskParams params = new UpdateGCMRegIdTaskParams(activity, String.valueOf(userModel.getUserId()), userModel.getGcmRegId());
                    new UpdateGCMRegIdTask(params, GCMController.this).execute();
                }
            }
        }
    }


    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_WITH_GCM:
                    new GCMRegistrationTask().execute();
                    break;
                case MSG_REGISTER_WEB_SERVER_SUCCESS:
                    /*Toast.makeText(getApplicationContext(),
                            "registered with web server", Toast.LENGTH_LONG).show();*/
                    break;
                case MSG_REGISTER_WEB_SERVER_FAILURE:
                    Toast.makeText(activity.getApplicationContext(),
                            "Registration with web server failed",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        };
    };
}
