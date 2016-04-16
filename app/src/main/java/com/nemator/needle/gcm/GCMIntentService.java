package com.nemator.needle.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.NotificationController;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;

import java.util.concurrent.atomic.AtomicInteger;

public class GCMIntentService extends IntentService {

    public static final String TAG = "GCMIntentService";
    public static final int NOTIFICATION_ID = 1000;

    NotificationManager mNotificationManager;

    public GCMIntentService() {
        super(GCMIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {

            // read extras as sent from server
            String notification = extras.getString("notification");
            if(notification != null && !notification.isEmpty()){
                NotificationController.sendNotification(getApplicationContext(), extras);
            }else{
                String registrationId = extras.getString("registration_id");

                String regId = Needle.userModel.getGcmRegId();
                if((regId == null || regId.isEmpty()) && !regId.equals(registrationId)){
                    getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE).edit().
                            putString(AppConstants.TAG_GCM_REG_ID, registrationId).
                            putBoolean(AppConstants.TAG_GCM_REGISTERD, true).
                            commit();
                }else{
                    getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE).edit().
                            putBoolean(AppConstants.TAG_GCM_REGISTERD, true).
                            commit();
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }
}