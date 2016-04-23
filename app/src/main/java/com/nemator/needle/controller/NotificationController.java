package com.nemator.needle.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationController {

    public static final String TAG = "NotificationController";

    public static final int NOTIFICATION_ID = 1000;

    public static void sendNotification(Context context, Bundle data) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification
        Gson gson = new GsonBuilder()/*.registerTypeAdapter(HaystackVO.class, new HaystackVO.Deserializer())*/.create();
        NotificationVO notification = gson.fromJson(data.getString(AppConstants.TAG_NOTIFICATION), NotificationVO.class);

        //Intent
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
        }else if(notification.getType() == AppConstants.USER_LOCATION_SHARING || notification.getType() == AppConstants.USER_SHARING_LOCATION_BACK){
            intent = new Intent(context, LocationSharingActivity.class);
            LocationSharingVO locationSharing = gson.fromJson(data.getString("data"), LocationSharingVO.class);
            intent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Serializable) locationSharing );
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else if(notification.getType() == AppConstants.USER_CANCELLED_LOCATION_SHARING || notification.getType() == AppConstants.USER_STOPPED_SHARING_LOCATION){
            intent = new Intent(context, HomeActivity.class);
            intent.putExtra(AppConstants.TAG_SECTION, AppConstants.SECTION_LOCATION_SHARING_LIST);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_NOTIFICATION);
        }else{
            intent = new Intent(context, HomeActivity.class);
            intent.putExtra(AppConstants.TAG_ACTION, AppConstants.TAG_SECTION);
        }

        intent.putExtra(AppConstants.TAG_TYPE, notification.getType());
        intent.putExtra(AppConstants.TAG_ID, notification.getDataId());
        intent.setAction(AppConstants.TAG_NOTIFICATION);

        //Dispatch intent for other Activities
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(intent);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);

        //Send actual notification
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_app_white)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getDescription()))
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
}
