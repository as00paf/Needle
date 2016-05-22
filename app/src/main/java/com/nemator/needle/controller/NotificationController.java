package com.nemator.needle.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.activities.NeedleActivity;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationController {

    public static final String TAG = "NotificationController";

    public static final int NOTIFICATION_ID = 1000;

    private static final long[] VIBRATION_PATTERN = new long[]{ 6000, 1400 };

    public static void sendNotification(Context context, Bundle data) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Preferences
        SharedPreferences pref = context.getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE);
        Boolean showNotification = pref.getBoolean(context.getString(R.string.pref_key_show_notifications), true);

        //Notification
        Gson gson = new GsonBuilder()/*.registerTypeAdapter(HaystackVO.class, new HaystackVO.Deserializer())*/.create();
        NotificationVO notification = gson.fromJson(data.getString(AppConstants.TAG_NOTIFICATION), NotificationVO.class);
        Intent intent = createIntentFromData(context, data, notification, gson);

        if(showNotification){
            //Preferences
            Boolean playRingtone = pref.getBoolean(context.getString(R.string.pref_key_ringtone_notifications), true);
            Boolean pulseLight = pref.getBoolean(context.getString(R.string.pref_key_led_notifications), true);
            Boolean vibrate = pref.getBoolean(context.getString(R.string.pref_key_vibrate_notifications), true);

            //Pending intent for notification
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //Ringtone
            Uri ringtonURI = playRingtone ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : null;//TODO : replace default URI

            //Send Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_app_white)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getDescription()))
                    .setContentText(notification.getDescription())
                    .setColor(context.getResources().getColor(R.color.primary))
                    .setLights(context.getResources().getColor(R.color.primary), 1500, 2000)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(ringtonURI)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);

            int id = new AtomicInteger(NOTIFICATION_ID).incrementAndGet();
            mNotificationManager.notify(id, mBuilder.build());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mBuilder.setPriority(Notification.PRIORITY_HIGH);
            }

            //Light
            if(pulseLight){
                mBuilder.setLights(ContextCompat.getColor(context, R.color.primary), 600, 300);
            }

            //Vibration
            if(vibrate){
                mBuilder.setVibrate(VIBRATION_PATTERN);
            }

            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }else{
            Log.e(TAG, "Not sending notification" );
        }

        //Dispatch intent for other Activities
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(intent);
    }

    private static Intent createIntentFromData(Context context, Bundle data, NotificationVO notification, Gson gson){
        Intent intent;
        if(notification.getType() == AppConstants.HAYSTACK_INVITATION || notification.getType() == AppConstants.USER_JOINED_HAYSTACK
                || notification.getType() == AppConstants.USER_LEFT_HAYSTACK){
            intent = new Intent(context, HaystackActivity.class);
            HaystackVO haystack = gson.fromJson(data.getString("data"), HaystackVO.class);
            intent.putExtra(AppConstants.TAG_HAYSTACK, (Serializable) haystack);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else if(notification.getType() == AppConstants.HAYSTACK_CANCELLED){
            intent = new Intent(context, HomeActivity.class);
            intent.putExtra(AppConstants.TAG_SECTION, AppConstants.SECTION_HAYSTACKS);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else if(notification.getType() == AppConstants.USER_NEEDLE || notification.getType() == AppConstants.USER_NEEDLE_BACK){
            intent = new Intent(context, NeedleActivity.class);
            NeedleVO locationSharing = gson.fromJson(data.getString("data"), NeedleVO.class);
            intent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Serializable) locationSharing );
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else if(notification.getType() == AppConstants.USER_CANCELLED_NEEDLE || notification.getType() == AppConstants.USER_STOPPED_NEEDLE){
            intent = new Intent(context, HomeActivity.class);
            intent.putExtra(AppConstants.TAG_SECTION, AppConstants.SECTION_NEEDLES);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else{
            intent = new Intent(context, HomeActivity.class);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_SECTION);
        }

        intent.putExtra(AppConstants.TAG_TYPE, notification.getType());
        intent.putExtra(AppConstants.TAG_ID, notification.getDataId());
        intent.setAction(AppConstants.TAG_NOTIFICATION);

        return intent;
    }
}
