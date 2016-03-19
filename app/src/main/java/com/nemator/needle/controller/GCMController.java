package com.nemator.needle.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.utils.AppConstants;

import java.io.IOException;

public class GCMController /*implements UserTask.UpdateGCMIDResponseHandler*/ {

    public static String TAG = "GCMController";

    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private static final String GCM_SENDER_ID = "648034739265";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static GCMController instance;

    private AppCompatActivity activity;

    GoogleCloudMessaging gcm;
    int tryCount = 0;

    public GCMController(){

    }

    public static GCMController getInstance() {
        if(instance == null){
            instance = new GCMController();
        }

        return instance;
    }

    public void init(AppCompatActivity activity){
        this.activity = activity;
        Needle.userModel.init(activity);

        //GCM
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());

            // Read saved registration id from shared preferences.
            Needle.userModel.setGcmRegId(activity.getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE).getString(AppConstants.TAG_GCM_REG_ID, ""));

            if (TextUtils.isEmpty(Needle.userModel.getGcmRegId())) {
                handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            }else{
                new RegisterGCMTask().execute();
            }
        }
    }

    public boolean checkPlayServices() {
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

    /*@Override
   public void onGCMIDUpdated(UserResult result) {
        if(result.successCode != 1){//Retry
            tryCount++;
            if(tryCount < 3){
                UserTaskParams params = new UserTaskParams(activity, UserTaskParams.TYPE_UPDATE_GCM_ID, Needle.userModel.getUser());
                new UserTask(params, GCMController.this).execute();
            }else{
                Toast.makeText(activity, "Error While Updating GCM RegID", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    class RegisterGCMTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Already registered with GCM
            try {
                Needle.userModel.setGcmRegId(gcm.register(GCM_SENDER_ID));
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
                return Needle.userModel.setGcmRegId(gcm.register(activity.getResources().getString(R.string.gcm_defaultSenderId)));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                Log.i(TAG, "Was already registered with GCM " + Needle.userModel.getGcmRegId());
            }else{
                Log.i(TAG, "Needs to register with GCM " + Needle.userModel.getGcmRegId());

                if(Needle.userModel.getUserId() != -1){
                    //UserTaskParams params = new UserTaskParams(activity, UserTaskParams.TYPE_UPDATE_GCM_ID, Needle.userModel.getUser());
                    //new UserTask(params, GCMController.this).execute();
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
