package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookCoverVO implements Serializable, Parcelable {

    @SerializedName("source")
    private String source;

    protected FacebookCoverVO(Parcel in) {
        source = in.readString();
    }

    public static final Creator<FacebookCoverVO> CREATOR = new Creator<FacebookCoverVO>() {
        @Override
        public FacebookCoverVO createFromParcel(Parcel in) {
            return new FacebookCoverVO(in);
        }

        @Override
        public FacebookCoverVO[] newArray(int size) {
            return new FacebookCoverVO[size];
        }
    };

    //Getters/Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
    }
}
