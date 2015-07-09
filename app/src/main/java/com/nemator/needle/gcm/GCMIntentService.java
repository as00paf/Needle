package com.nemator.needle.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.utils.AppConstants;

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
            String message = extras.getString("message");
            if(message != null && !message.isEmpty()){
                sendNotification( message);
            }else{
                String registrationId = extras.getString("registration_id");

                String regId = PreferenceManager.getDefaultSharedPreferences(this).getString(AppConstants.TAG_GCM_REG_ID, "");
                if(regId == null || regId.isEmpty()){
                    PreferenceManager.getDefaultSharedPreferences(this).edit().
                            putString(AppConstants.TAG_GCM_REG_ID, registrationId).
                            putBoolean(AppConstants.TAG_GCM_REGISTERD, true).
                            commit();
                }else{
                    PreferenceManager.getDefaultSharedPreferences(this).edit().
                            putBoolean(AppConstants.TAG_GCM_REGISTERD, true).
                            commit();
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setColor(getResources().getColor(R.color.primary))
                .setLights(getResources().getColor(R.color.primary), 1500, 2000)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSound(defaultSoundUri);


        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}