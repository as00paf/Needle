package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationVO implements Parcelable, Serializable {

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    public LocationVO() {

    }

    public LocationVO(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationVO(LatLng position) {
        this.latitude = position.latitude;
        this.longitude = position.longitude;
    }

    protected LocationVO(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Creator<LocationVO> CREATOR = new Creator<LocationVO>() {
        @Override
        public LocationVO createFromParcel(Parcel in) {
            return new LocationVO(in);
        }

        @Override
        public LocationVO[] newArray(int size) {
            return new LocationVO[size];
        }
    };

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
