package com.nemator.needle.tasks.addPostLocationRequest;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.nemator.needle.broadcastReceiver.PostLocationRequestAlarm;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.tasks.TaskResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPostLocationRequestTask extends AsyncTask<Void, Void, TaskResult> {
    private static final String TAG = "AddPostLocationRequestTask";

    private AddPostLocationRequestHandler delegate;
    private AddPostLocationRequestParams params;

    private LocationServiceDBHelper dbHelper;


    public AddPostLocationRequestTask(AddPostLocationRequestParams params, AddPostLocationRequestHandler delegate){
        this.params = params;
        this.delegate = delegate;
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

        // Create a new map of values, where column names are the keys

        //Date/Time Limit
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int  hours = calendar.getTime().getHours()+1;
        int minutes = calendar.getTime().getMinutes() + 10;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        String currentDate = sdf.format(new Date(year-1900, month, day, hours, minutes));

        ContentValues values = new ContentValues();
        values.put(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_TYPE, params.type);
        values.put(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_DATE, currentDate);
        values.put(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_EXPIRATION, params.expiration);
        values.put(LocationServiceDBHelper.PostLocationRequest.COLUMN_NAME_POSTER_ID, params.posterId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                LocationServiceDBHelper.PostLocationRequest.TABLE_NAME,
                null,
                values);

        //ALARM
        new PostLocationRequestAlarm().setAlarm(params.context, newRowId, params.expiration, params.posterId);

        return result;
    }

    protected void onPostExecute(TaskResult result) {
        delegate.onLocationRequestPosted(result);
    }

    public interface AddPostLocationRequestHandler {
        void onLocationRequestPosted(TaskResult result);
    }
}