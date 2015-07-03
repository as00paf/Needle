package com.nemator.needle.tasks.removePostLocationRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;

import com.nemator.needle.broadcastReceiver.PostLocationRequestAlarm;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.retrieveLocations.RetrieveLocationsResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RemovePostLocationRequestTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String TAG = "RemovePostLocationRequestTask";

    private AddPostLocationRequestParams params;

    private LocationServiceDBHelper dbHelper;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public RemovePostLocationRequestTask(AddPostLocationRequestParams params){
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dbHelper = new LocationServiceDBHelper(params.context);
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        //DB ENTRY
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID + " = " + String.valueOf(params.posterId);

        int success = db.delete(LocationServiceDBHelper.PostLocationRequest.TABLE_NAME, selection, null);
        result.successCode = success;

        //ALARM
        Intent intent = new Intent(params.context, PostLocationRequestAlarm.class);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest._ID, params.rowId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID, params.posterId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION, params.expiration);
        alarmIntent = PendingIntent.getBroadcast(params.context, 0, intent, 0);
        alarmMgr = (AlarmManager) params.context.getSystemService(params.context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);

        return result;
    }
}