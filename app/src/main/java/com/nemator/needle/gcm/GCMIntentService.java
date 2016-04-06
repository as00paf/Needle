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
                sendNotification(extras);
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

    private void sendNotification(Bundle data) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Gson gson = new Gson();
        NotificationVO notification = gson.fromJson(data.getString("notification"), NotificationVO.class);

        Intent intent = createIntent(notification);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_app_white)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getTitle()))
                .setContentText(notification.getDescription())
                .setColor(getResources().getColor(R.color.primary))
                .setLights(getResources().getColor(R.color.primary), 1500, 2000)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        int id = new AtomicInteger(NOTIFICATION_ID).incrementAndGet();
        mNotificationManager.notify(id, mBuilder.build());
    }

    private Intent createIntent(NotificationVO notification){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(AppConstants.TAG_TYPE, notification.getType());
        intent.putExtra(AppConstants.TAG_ACTION, "Notification");
        intent.putExtra(AppConstants.TAG_ID, notification.getDataId());

        if(notification.getType() == 0){
            //intent.putExtra(AppConstants.LOCATION_SHARING_DATA_KEY, (Serializable) vo );
        }else if(notification.getType() == 1){
            //intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, data.getParcelable("haystack"));
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

}