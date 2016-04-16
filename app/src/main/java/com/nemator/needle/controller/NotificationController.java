package com.nemator.needle.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alex on 16/04/2016.
 */
public class NotificationController {

    public static final String TAG = "NotificationController";

    public static final int NOTIFICATION_ID = 1000;

    public static void sendNotification(Context context, Bundle data) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Gson gson = new Gson();
        NotificationVO notification = gson.fromJson(data.getString("notification"), NotificationVO.class);

        Intent intent = createIntent(context, notification);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_app_white)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getTitle()))
                .setContentText(notification.getDescription())
                .setColor(context.getResources().getColor(R.color.primary))
                .setLights(context.getResources().getColor(R.color.primary), 1500, 2000)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        int id = new AtomicInteger(NOTIFICATION_ID).incrementAndGet();
        mNotificationManager.notify(id, mBuilder.build());
    }

    private static Intent createIntent(Context context, NotificationVO notification){
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra(AppConstants.TAG_TYPE, notification.getType());
        intent.putExtra(AppConstants.TAG_ACTION, "Notification");
        intent.putExtra(AppConstants.TAG_ID, notification.getDataId());

        if(notification.getType() == 0){
            //intent.putExtra(AppConstants.LOCATION_SHARING_DATA_KEY, (Serializable) vo );
        }else if(notification.getType() == 1){
            //intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, data.getParcelable("haystack"));
        }

        ;

        return intent;
    }
}
