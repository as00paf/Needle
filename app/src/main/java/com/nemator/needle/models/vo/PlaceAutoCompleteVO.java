package com.nemator.needle.models.vo;

/**
 * Created by Alex on 14/05/2015.
 */
public class PlaceAutoCompleteVO {

    public CharSequence placeId;
    public CharSequence description;

    public PlaceAutoCompleteVO(CharSequence placeId, CharSequence description) {
        this.placeId = placeId;
        this.description = description;
    }

    @Override
    public String toString() {
        return description.toString();
    }
}