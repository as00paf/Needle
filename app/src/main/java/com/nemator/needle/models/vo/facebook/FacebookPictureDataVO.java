package com.nemator.needle.models.vo.facebook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookPictureDataVO implements Serializable, Parcelable {
    @SerializedName("url")
    private String url;

    protected FacebookPictureDataVO(Parcel in) {
        url = in.readString();
    }

    public static final Creator<FacebookPictureDataVO> CREATOR = new Creator<FacebookPictureDataVO>() {
        @Override
        public FacebookPictureDataVO createFromParcel(Parcel in) {
            return new FacebookPictureDataVO(in);
        }

        @Override
        public FacebookPictureDataVO[] newArray(int size) {
            return new FacebookPictureDataVO[size];
        }
    };

    public FacebookPictureDataVO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
}
