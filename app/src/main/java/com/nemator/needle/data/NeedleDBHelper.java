package com.nemator.needle.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.nemator.needle.models.vo.CustomPlace;

import java.util.ArrayList;

/**
 * Created by Alex on 29/06/2015.
 */
public class NeedleDBHelper extends SQLiteOpenHelper {
    private static String TAG = "NeedleDBHelper";

    private static final String DB_NAME = "Needle";
    private static final int VERSION = 4;

    private static NeedleDBHelper sDBHelper;
    private Context mContext;

    private NeedleDBHelper(Context context)
    {
        super(context, DB_NAME, null, VERSION);
        mContext = context;
    }

    public static NeedleDBHelper getInstance(Context context)
    {
        if (sDBHelper == null)
        {
            sDBHelper = new NeedleDBHelper(context.getApplicationContext());
        }

        return sDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Search History table
        db.execSQL("CREATE TABLE " + DBConstants.SEARCH_HISTORY_TABLE_NAME +
                " (" + DBConstants.COLUMN_PLACE_ID + " string primary key" +
                ", " + DBConstants.COLUMN_TITLE + " string" +
                ", " + DBConstants.COLUMN_SUBTITLE + " string" +
                ", " + DBConstants.COLUMN_LATITUDE + " float" +
                ", " + DBConstants.COLUMN_LONGITUDE + " float);");

        Log.d(TAG, "DB created !");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBConstants.SEARCH_HISTORY_TABLE_NAME);

        onCreate(sqLiteDatabase);
    }

    public void deleteDatabase()
    {
        mContext.deleteDatabase(DB_NAME);
    }

    //Search History
    public void insertSearchHistoryItem(CustomPlace item)
    {
        Cursor cursor = getReadableDatabase().query(
                DBConstants.SEARCH_HISTORY_TABLE_NAME, null, null, null, null, null, null
        );

        //Limit to 5 items
        if(cursor.getCount() > 4){
            CustomPlace.CustomPlaceCursorWrapper wrapper = new CustomPlace.CustomPlaceCursorWrapper(cursor);
            String title = wrapper.getItemTitleAt(0);

            String selectionString = DBConstants.COLUMN_TITLE + "=?";
            String[] selectionParams = new String[]{title};
            getWritableDatabase().delete(
                    DBConstants.SEARCH_HISTORY_TABLE_NAME,
                    selectionString,
                    selectionParams);
        }

        getWritableDatabase().insert(
                DBConstants.SEARCH_HISTORY_TABLE_NAME,
                null,
                item.contentValuesForItem());

        cursor.close();
    }

    public ArrayList<CustomPlace> getSearchHistory(){
        Cursor cursor = getReadableDatabase().query(
                DBConstants.SEARCH_HISTORY_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        CustomPlace.CustomPlaceCursorWrapper cursorWrapper = new CustomPlace.CustomPlaceCursorWrapper(cursor);

        ArrayList<CustomPlace> items = cursorWrapper.getAllItems();
        cursor.close();

        return items;
    }
}
