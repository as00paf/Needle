package com.nemator.needle.models.vo;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import retrofit2.http.Field;

public class ImageVO implements Serializable {

    @SerializedName("userId")
    private int userId;

    @SerializedName("photo")
    private String photo;

    @SerializedName("type")
    private String pictureType;

    @SerializedName("url")
    private String pictureURL;


    public ImageVO() {

    }

    public ImageVO(int userId, String photo, String pictureType) {
        this.userId = userId;
        this.photo = photo;
        this.pictureType = pictureType;
    }

    public ImageVO(String pictureURL, int userId, String type) {
        this.pictureURL = pictureURL;
        this.userId = userId;
        this.pictureType = type;
    }

    //Getters/Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPictureType() {
        return pictureType;
    }

    public void setPictureType(String pictureType) {
        this.pictureType = pictureType;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }
}
