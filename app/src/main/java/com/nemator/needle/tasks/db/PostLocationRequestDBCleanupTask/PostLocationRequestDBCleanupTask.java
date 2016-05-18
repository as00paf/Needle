package com.nemator.needle.tasks.db.PostLocationRequestDBCleanupTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.broadcastReceiver.PostLocationRequestAlarm;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.data.PostLocationRequest;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.utils.AppUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class PostLocationRequestDBCleanupTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String TAG = "PostLocCleanupTask";
    private final Context context;

    private LocationServiceDBHelper dbHelper;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public PostLocationRequestDBCleanupTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dbHelper = LocationServiceDBHelper.getInstance(context);
        Log.i(TAG, "Starting PostLocationRequestCleanup task ");
    }

    @Override
    protected TaskResult doInBackground(Void... args) {
        TaskResult result = new TaskResult();

        //Get all entries
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                PostLocationRequest._ID,
                PostLocationRequest.COLUMN_NAME_ITEM_ID,
                PostLocationRequest.COLUMN_NAME_TYPE,
                PostLocationRequest.COLUMN_NAME_DATE,
                PostLocationRequest.COLUMN_NAME_EXPIRATION,
                PostLocationRequest.COLUMN_NAME_POSTER_ID,
        };

        Cursor cursor = db.query(
                PostLocationRequest.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        cursor.moveToFirst();
        int count = cursor.getCount();
        ArrayList<PostLocationRequest> toDelete = new ArrayList<>();

        if (count > 0) {
            Log.d(TAG, "Has " + count + " Post Location Requests");

            do {
                PostLocationRequest request = new PostLocationRequest(cursor);
                if (AppUtils.isDateAfterNow(request.getExpiration(), "yyyy-MM-dd hh:mm")) {
                    toDelete.add(request);
                }else{
                    Log.d(TAG, "Date " + request.getExpiration() + " is not after now");
                }
            } while (cursor.moveToNext());

        } else {
            result.setMessage("DB is empty");
            result.setSuccessCode(1);
            return result;
        }

        //Cleanup
        if (toDelete.size() > 0) {
            db = dbHelper.getWritableDatabase();

            Log.d(TAG, "Deleting " + count + " requests.");

            for (PostLocationRequest request : toDelete) {
                String whereClause = PostLocationRequest._ID + "=?";
                String[] whereArgs = {String.valueOf(request.getId())};

                int success = db.delete(PostLocationRequest.TABLE_NAME, whereClause, whereArgs);
                result.setSuccessCode(success);

                //ALARM
                Intent intent = new Intent(context, PostLocationRequestAlarm.class);
                intent.putExtra(PostLocationRequest._ID, request.getId());
                intent.putExtra(PostLocationRequest.COLUMN_NAME_POSTER_ID, request.getPosterId());
                intent.putExtra(PostLocationRequest.COLUMN_NAME_EXPIRATION, request.getExpiration());
                alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                alarmMgr.cancel(alarmIntent);

                if(success > 0){
                    Log.d(TAG, "Deleted request with id " + request.getId());
                }else{
                    Log.d(TAG, "Could not delete request with id " + request.getId());
                    result.setMessage(result.getMessage() + "Could not delete request with id : " + request.getId());
                    result.setSuccessCode(2);
                }
            }
        }else{
            result.setSuccessCode(1);
            result.setMessage("Success");
            Log.d(TAG, "No request to delete");
        }

        return result;
    }
}