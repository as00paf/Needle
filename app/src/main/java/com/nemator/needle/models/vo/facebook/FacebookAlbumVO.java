package com.nemator.needle.models.vo.facebook;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookAlbumVO implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("created_time")
    private String createdTime;

    @SerializedName("picture")
    private FacebookPictureVO picture;

    @SerializedName("count")
    private String count;

    @SerializedName("photos")
    private FacebookPicturesVO photos;

    public FacebookAlbumVO() {
    }

    //Getters/Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public FacebookPictureVO getPicture() {
        return picture;
    }

    public void setPicture(FacebookPictureVO picture) {
        this.picture = picture;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public FacebookPicturesVO getPhotos() {
        return photos;
    }

    public void setPhotos(FacebookPicturesVO photos) {
        this.photos = photos;
    }
}
