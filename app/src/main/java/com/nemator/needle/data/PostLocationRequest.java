package com.nemator.needle.data;

import android.database.Cursor;
import android.provider.BaseColumns;

public class PostLocationRequest implements BaseColumns {
    public static final String TABLE_NAME = "NeedleLocationServicePostRequest";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_EXPIRATION = "expiration";
    public static final String COLUMN_NAME_POSTER_ID = "posterId";
    public static final String COLUMN_NAME_ITEM_ID = "itemId";

    public static final String[] PROJECTION = {
            PostLocationRequest._ID,
            PostLocationRequest.COLUMN_NAME_TYPE,
            PostLocationRequest.COLUMN_NAME_DATE,
            PostLocationRequest.COLUMN_NAME_EXPIRATION,
            PostLocationRequest.COLUMN_NAME_POSTER_ID,
            PostLocationRequest.COLUMN_NAME_ITEM_ID
    };

    private int id;
    private int type;
    private String date;
    private String expiration;
    private int posterId;
    private int itemId;

    public PostLocationRequest() {
    }

    public PostLocationRequest(int id, int type, String date, String expiration, int posterId, int itemId) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.expiration = expiration;
        this.posterId = posterId;
        this.itemId = itemId;
    }

    public PostLocationRequest(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(_ID));
        this.type = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TYPE));
        this.date = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DATE));
        this.expiration = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_EXPIRATION));
        this.posterId = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_POSTER_ID));
        this.itemId = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ITEM_ID));
    }

    //Getters/Setters
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public int getPosterId() {
        return posterId;
    }

    public void setPosterId(int posterId) {
        this.posterId = posterId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static class Type{
        public static final int HAYSTACK = 0;
        public static final int NEEDLE = 1;
        public static final int NEEDLE_BACK = 2;
    }

}