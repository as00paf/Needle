package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PinVO implements Parcelable, Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("ownerId")
    private int ownerId;

    @SerializedName("haystackId")
    private int haystackId;

    @SerializedName("text")
    private String text;

    @SerializedName("addedAt")
    private String addedAt;

    public PinVO() {

    }

    public PinVO(int id, Double latitude, Double longitude, int ownerId, int haystackId, String text, String addedAt) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ownerId = ownerId;
        this.haystackId = haystackId;
        this.text = text;
        this.addedAt = addedAt;
    }

    protected PinVO(Parcel in) {
        this.id = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.ownerId = in.readInt();
        this.haystackId = in.readInt();
        this.text = in.readString();
        this.addedAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(ownerId);
        dest.writeInt(haystackId);
        dest.writeString(text);
        dest.writeString(addedAt);
    }

    public static final Creator<PinVO> CREATOR = new Creator<PinVO>() {
        @Override
        public PinVO createFromParcel(Parcel in) {
            return new PinVO(in);
        }

        @Override
        public PinVO[] newArray(int size) {
            return new PinVO[size];
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

    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getHaystackId() {
        return haystackId;
    }

    public void setHaystackId(int haystackId) {
        this.haystackId = haystackId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
