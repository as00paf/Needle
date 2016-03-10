package com.nemator.needle.tasks.db.removePostLocationRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.broadcastReceiver.PostLocationRequestAlarm;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;

public class RemovePostLocationRequestTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String TAG = "RemovePostLocationTask";

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

        dbHelper = LocationServiceDBHelper.getInstance(params.context);
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        //DB ENTRY
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection =   LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID + " = " + String.valueOf(params.posterId) + " AND " +
                             LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_ITEM_ID + " = " + String.valueOf(params.itemId);

        Log.i(TAG, "selection : " + selection);

        int success = db.delete(LocationServiceDBHelper.PostLocationRequest.TABLE_NAME, selection, null);
        result.setSuccessCode(success);

        //ALARM
        Intent intent = new Intent(params.context, PostLocationRequestAlarm.class);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest._ID, params.rowId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID, params.posterId);
        intent.putExtra(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION, params.expiration);
        alarmIntent = PendingIntent.getBroadcast(params.context, 0, intent, 0);
        alarmMgr = (AlarmManager) params.context.getSystemService(params.context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);

        db = dbHelper.getReadableDatabase();
        String[] projection = {
                LocationServiceDBHelper.PostLocationRequest._ID,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_TYPE,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_DATE,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_ITEM_ID
        };

        Cursor cursor = db.query(
                LocationServiceDBHelper.PostLocationRequest.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        cursor.moveToFirst();

        if(cursor.getCount() > 0){
            Log.i(TAG, "Remaining requests : ");
            do{
                int i =0;
                for(String columnName : cursor.getColumnNames()){
                    Log.i(TAG, columnName + " = " + cursor.getString(i));
                    i++;
                }
            }while (cursor.moveToNext());
        }else{
            Log.i(TAG, "Removed all requests !");
        }

        return result;
    }
}