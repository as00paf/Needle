package com.nemator.needle.models.vo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.data.DBConstants;

import java.util.ArrayList;

/**
 * Created by Alex on 29/11/2015.
 */
public class CustomPlace {

    private String title;
    private String subtitle;
    private LatLng location;
    private String placeId;

    public CustomPlace() {
        super();
    }

    public CustomPlace(String placeId, String title, String subtitle, LatLng location) {
        this.placeId = placeId;
        this.title = title;
        this.subtitle = subtitle;
        this.location = location;
    }

    public CustomPlace(Place place){
        this.placeId = place.getId();
        this.title = place.getName().toString();
        this.subtitle = place.getAddress().toString();
        this.location = place.getLatLng();
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public ContentValues contentValuesForItem() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.COLUMN_PLACE_ID, placeId);
        contentValues.put(DBConstants.COLUMN_TITLE, title);
        contentValues.put(DBConstants.COLUMN_SUBTITLE, subtitle);

        if(location != null){
            contentValues.put(DBConstants.COLUMN_LATITUDE, location.latitude);
            contentValues.put(DBConstants.COLUMN_LONGITUDE, location.longitude);
        }

        return contentValues;
    }

    public static class CustomPlaceCursorWrapper extends CursorWrapper
    {
        public CustomPlaceCursorWrapper(Cursor cursor)
        {
            super(cursor);
        }

        public String getItemTitleAt(int position)
        {
            moveToPosition(position);

            return getString(getColumnIndex(DBConstants.COLUMN_TITLE));
        }

        public ArrayList<CustomPlace> getAllItems(){
            ArrayList<CustomPlace> items = new ArrayList<>();

            moveToFirst();

            int i;
            for(i=0; i < getCount() ; i++){
                CustomPlace place = new CustomPlace();
                place.placeId = getString(getColumnIndex(DBConstants.COLUMN_PLACE_ID));
                place.title = getString(getColumnIndex(DBConstants.COLUMN_TITLE));
                place.subtitle = getString(getColumnIndex(DBConstants.COLUMN_SUBTITLE));
                place.location = new LatLng(getDouble(getColumnIndex(DBConstants.COLUMN_LATITUDE)), getDouble(getColumnIndex(DBConstants.COLUMN_LONGITUDE)));

                items.add(place);
            }

            return items;
        }
    }

}
