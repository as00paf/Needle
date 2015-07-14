package com.nemator.needle.broadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.removePostLocationRequest.RemovePostLocationRequestTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Alex on 29/06/2015.
 */
public class PostLocationRequestAlarm extends BroadcastReceiver {
    public static final String TAG = "LocationRequestAlarm";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private Boolean pendingAlarm = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Params
        int type = intent.getIntExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_TYPE, -1);
        String expiration = intent.getStringExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION);
        int posterId = intent.getIntExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID, -1);
        String itemId = intent.getStringExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_ITEM_ID);

        AddPostLocationRequestParams params = new AddPostLocationRequestParams(context, type, expiration, posterId, itemId );
        params.rowId = intent.getLongExtra(LocationServiceDBHelper.PostLocationRequest._ID, -1);
        params.posterId = posterId;
        params.type = type;
        params.expiration = expiration;

        new RemovePostLocationRequestTask(params).execute();
    }

    public void setAlarm(Context context, long rowId, String expiration, int posterId, String itemId)
    {
        Log.i(TAG, "Setting Alarm @ " + expiration);

        Intent intent = new Intent(context, PostLocationRequestAlarm.class);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest._ID, rowId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID, posterId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION, expiration);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_ITEM_ID, itemId);

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        try {
            date = sdf.parse(expiration);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), alarmIntent);
    }

    public void CancelAlarm(Context context, long rowId)
    {
        Intent intent = new Intent(context, PostLocationRequestAlarm.class);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest._ID, rowId);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }

}
