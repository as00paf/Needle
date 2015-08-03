package com.nemator.needle.tasks.db.isPostLocationRequestDBEmpty;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.nemator.needle.data.LocationServiceDBHelper;

/**
 * Created by Alex on 29/06/2015.
 */
public class IsPostLocationRequestDBEmptyTask extends AsyncTask<Void, Void, Boolean> {
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
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                LocationServiceDBHelper.PostLocationRequest._ID,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_ITEM_ID,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_TYPE,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_DATE,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION,
                LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID,
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
        int count = cursor.getCount();

        return count > 0;
    }

    protected void onPostExecute(Boolean result) {
        delegate.onPostLocationRequestDBIsEmptyResult(result);
    }

    public interface IsPostLocationRequestDBEmptyResponseHandler {
        void onPostLocationRequestDBIsEmptyResult(Boolean result);
    }
}
