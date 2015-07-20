package com.nemator.needle.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Alex on 29/06/2015.
 */
public class LocationServiceDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DATE_TYPE = " datetime";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PostLocationRequest.TABLE_NAME + " (" +
                    PostLocationRequest._ID + " INTEGER PRIMARY KEY," +
                    PostLocationRequest.COLUMN_NAME_ITEM_ID + TEXT_TYPE + COMMA_SEP +
                    PostLocationRequest.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    PostLocationRequest.COLUMN_NAME_DATE + DATE_TYPE + COMMA_SEP +
                    PostLocationRequest.COLUMN_NAME_EXPIRATION + DATE_TYPE + COMMA_SEP +
                    PostLocationRequest.COLUMN_NAME_POSTER_ID + INT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PostLocationRequest.TABLE_NAME;

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "PostLocationRequest.db";

    public LocationServiceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public static abstract class PostLocationRequest implements BaseColumns {
        public static final String TABLE_NAME = "NeedleLocationServicePostRequest";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_EXPIRATION = "expiration";
        public static final String COLUMN_NAME_POSTER_ID = "posterId";
        public static final String COLUMN_NAME_ITEM_ID = "itemId";

        public static final int POSTER_TYPE_HAYSTACK = 0;
        public static final int POSTER_TYPE_LOCATION_SHARING = 1;
        public static final int POSTER_TYPE_LOCATION_SHARING_BACK = 2;

    }
}
