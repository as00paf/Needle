package com.nemator.needle.models.vo.facebook;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.utils.AppConstants;

import java.io.Serializable;

public class FacebookUserVO implements Serializable, Parcelable{

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("gender")
    private String gender;

    @SerializedName("picture")
    private FacebookPictureVO picture;

    @SerializedName("cover")
    private FacebookCoverVO cover;

    public FacebookUserVO(){

    }

    /**
     * @param id
     * @param name
     * @param pictureURL
     */
    public FacebookUserVO(String id, String name, String gender, FacebookPictureVO pictureURL){
        this.id = id;
        this.name = name;
        this.picture = pictureURL;
        this.gender = gender;
    }


    /**
     * @param in
     */
    public FacebookUserVO(Parcel in){
        id = in.readString();
        name = in.readString();
        email = in.readString();
        picture = in.readParcelable(FacebookPictureVO.class.getClassLoader());
        cover = in.readParcelable(FacebookCoverVO.class.getClassLoader());
        gender = in.readString();
    }

    public String getId() {
        return id;
    }

    public FacebookUserVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FacebookUserVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public FacebookUserVO setEmail(String email) {
        this.email = email;
        return this;
    }

    public FacebookPictureVO getPicture() {
        return picture;
    }

    public FacebookUserVO setPicture(FacebookPictureVO picture) {
        this.picture = picture;
        return this;
    }

    public FacebookCoverVO getCover() {
        return cover;
    }

    public FacebookUserVO setCover(FacebookCoverVO cover) {
        this.cover = cover;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public FacebookUserVO setGender(String gender) {
        this.gender = gender;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeParcelable(picture, flags);
        dest.writeParcelable(cover, flags);
        dest.writeString(gender);
    }

    public static final Creator<FacebookUserVO> CREATOR = new Creator<FacebookUserVO>() {

        @Override
        public FacebookUserVO createFromParcel(Parcel source) {
            return new FacebookUserVO(source);
        }

        @Override
        public FacebookUserVO[] newArray(int size) {
            return new FacebookUserVO[size];
        }
    };

    public void save(SharedPreferences sharedPreferences) {
        sharedPreferences.edit()
                .putString(AppConstants.TAG_FB_USER_ID, id)
                .putString(AppConstants.TAG_FB_USER_NAME, name)
                .putString(AppConstants.TAG_FB_EMAIL, email)
                .putString(AppConstants.TAG_FB_PICTURE_URL, picture.getData().getUrl())
                .putString(AppConstants.TAG_FB_PICTURE_URL, cover.getSource())
                .putString(AppConstants.TAG_FB_GENDER, gender)
                .commit();
    }

    public static FacebookUserVO retrieve(SharedPreferences mSharedPreferences){
        FacebookUserVO vo = new FacebookUserVO();

        vo.id = mSharedPreferences.getString(AppConstants.TAG_FB_USER_ID, null);
        vo.name = mSharedPreferences.getString(AppConstants.TAG_FB_USER_NAME, null);
        vo.email = mSharedPreferences.getString(AppConstants.TAG_FB_EMAIL, null);
        vo.email = mSharedPreferences.getString(AppConstants.TAG_FB_PICTURE_URL, null);
        vo.picture = new FacebookPictureVO(mSharedPreferences.getString(AppConstants.TAG_FB_PICTURE_URL, null));
        vo.cover = new FacebookCoverVO(mSharedPreferences.getString(AppConstants.TAG_COVER_FB_PICTURE_URL, null));
        vo.gender = mSharedPreferences.getString(AppConstants.TAG_FB_GENDER, null);

        return vo;
    }
}
