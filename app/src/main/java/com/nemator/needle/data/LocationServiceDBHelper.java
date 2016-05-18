package com.nemator.needle.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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

    private static LocationServiceDBHelper mInstance = null;

    public static LocationServiceDBHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new LocationServiceDBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private LocationServiceDBHelper(Context context) {
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
}
