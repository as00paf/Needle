package com.nemator.needle.tasks.db.isPostLocationRequestDBEmpty;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.data.PostLocationRequest;
import com.nemator.needle.tasks.db.PostLocationRequestDBCleanupTask.PostLocationRequestDBCleanupTask;

public class IsPostLocationRequestDBEmptyTask extends AsyncTask<Void, Void, Boolean> {
    public static String TAG = "HasPostLocRequest";

    private IsPostLocationRequestDBEmptyResponseHandler delegate;
    private Context context;

    private LocationServiceDBHelper dbHelper;

    public IsPostLocationRequestDBEmptyTask(Context context, IsPostLocationRequestDBEmptyResponseHandler delegate){
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dbHelper = LocationServiceDBHelper.getInstance(context);

        //DB Cleanup !
        new PostLocationRequestDBCleanupTask(context).execute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
        int count = cursor.getCount();

        if(count > 0){
            Log.d(TAG, "Has " + count + " Post Location Requests");
            do{
               PostLocationRequest request = new PostLocationRequest(cursor);
                Log.d(TAG, "Request with id " + request.getId() + " ends @ " + request.getExpiration());
            }while (cursor.moveToNext());
        }

        return count > 0;
    }

    protected void onPostExecute(Boolean result) {
        delegate.onPostLocationRequestDBIsEmptyResult(result);
    }

    public interface IsPostLocationRequestDBEmptyResponseHandler {
        void onPostLocationRequestDBIsEmptyResult(Boolean result);
    }
}
