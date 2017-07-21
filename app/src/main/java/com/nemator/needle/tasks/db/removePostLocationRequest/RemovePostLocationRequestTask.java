package com.nemator.needle.tasks.db.removePostLocationRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.Needle;
import com.nemator.needle.broadcastReceiver.PostLocationRequestAlarm;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.data.PostLocationRequest;
import com.nemator.needle.tasks.db.PostLocationRequestDBCleanupTask.PostLocationRequestDBCleanupTask;
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
        new PostLocationRequestDBCleanupTask(params.context).execute();
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        //DB ENTRY
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where;
        String[] whereArgs;

        if(params.id > -1){
            where =   PostLocationRequest._ID + " =?";
            whereArgs = new String[]{String.valueOf(params.id)};
        }else{
            where =   PostLocationRequest.COLUMN_NAME_POSTER_ID + " =? AND " +  PostLocationRequest.COLUMN_NAME_ITEM_ID +"=?";
            whereArgs = new String[]{String.valueOf(params.posterId), params.itemId};
        }

        int success = db.delete(PostLocationRequest.TABLE_NAME, where, whereArgs);
        result.setSuccessCode(success);

        if(success > 0){
            Log.i(TAG, "Post location request removed from db");
        }else{
            String msg =  "Could not remove post location request from db";
            Log.e(TAG, msg);

            throw new Error(msg);
        }

        //ALARM
        Intent intent = new Intent(params.context, PostLocationRequestAlarm.class);
        intent.putExtra(PostLocationRequest._ID, params.id);
        intent.putExtra(PostLocationRequest.COLUMN_NAME_POSTER_ID, params.posterId);
        intent.putExtra(PostLocationRequest.COLUMN_NAME_EXPIRATION, params.expiration);
        alarmIntent = PendingIntent.getBroadcast(params.context, 0, intent, 0);
        alarmMgr = (AlarmManager) params.context.getSystemService(params.context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PostLocationRequest.TABLE_NAME,  // The table to query
                PostLocationRequest.PROJECTION,                               // The columns to return
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