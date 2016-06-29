package com.nemator.needle.models.vo.facebook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookPictureVO implements Serializable, Parcelable {

    @SerializedName("data")
    private FacebookPictureDataVO data;

    protected FacebookPictureVO(Parcel in) {
        data = in.readParcelable(FacebookPictureDataVO.class.getClassLoader());
    }

    public FacebookPictureVO(String url) {
        data = new FacebookPictureDataVO(url);
    }

    public static final Creator<FacebookPictureVO> CREATOR = new Creator<FacebookPictureVO>() {
        @Override
        public FacebookPictureVO createFromParcel(Parcel in) {
            return new FacebookPictureVO(in);
        }

        @Override
        public FacebookPictureVO[] newArray(int size) {
            return new FacebookPictureVO[size];
        }
    };

    public FacebookPictureDataVO getData() {
        return data;
    }

    public void setData(FacebookPictureDataVO data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
    }
}
