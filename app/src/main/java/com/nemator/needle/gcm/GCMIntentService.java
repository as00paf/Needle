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
import com.nemator.needle.models.UserModel;
import com.nemator.needle.utils.AppConstants;

import org.json.JSONObject;

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
            String message = extras.getString("message");
            String type = extras.getString("notificationType");
            if(message != null && !message.isEmpty()){
                sendNotification( message, type, extras);
            }else{
                String registrationId = extras.getString("registration_id");

                UserModel userModel = new UserModel(this);
                String regId = userModel.getGcmRegId();
                if((regId == null || regId.isEmpty()) && !regId.equals(registrationId)){
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

    private void sendNotification(String msg, String type, Bundle data) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = createIntent(type, data);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setColor(getResources().getColor(R.color.primary))
                .setLights(getResources().getColor(R.color.primary), 1500, 2000)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        int id = new AtomicInteger(NOTIFICATION_ID).incrementAndGet();
        mNotificationManager.notify(id, mBuilder.build());
    }

    private Intent createIntent(String notificationType, Bundle data){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(AppConstants.TAG_TYPE, notificationType);
        intent.putExtra(AppConstants.TAG_ACTION, "Notification");
        intent.putExtra(AppConstants.TAG_ID, data.getString("id"));

        if(notificationType.equals("LocationSharing")){
            intent.putExtra(AppConstants.TAG_SENDER_NAME, data.getString(AppConstants.TAG_SENDER_NAME));
            intent.putExtra(AppConstants.TAG_SENDER_ID, data.getString(AppConstants.TAG_SENDER_ID));
            intent.putExtra(AppConstants.TAG_TIME_LIMIT, data.getString(AppConstants.TAG_TIME_LIMIT));
            intent.putExtra(AppConstants.TAG_SHARE_BACK, data.getString(AppConstants.TAG_SHARE_BACK));
        }else{

        }

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

}