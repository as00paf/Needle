package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NotificationVO implements Serializable, Parcelable {

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private int type;

    @SerializedName("userId")
    private int userId;

    @SerializedName("dataId")
    private int dataId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("sentAt")
    private String sentAt;

    @SerializedName("seen")
    private Boolean seen;

    @SerializedName("senderId")
    private int senderId;

    @SerializedName("senderPictureURL")
    private String senderPictureURL;

    @SerializedName("senderName")
    private String senderName;

    public NotificationVO(){

    }

    public NotificationVO(int id, int type, int userId, int dataId, String title, String description, String sentAt, Boolean seen, int senderId, String senderPictureURL, String senderName){
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.dataId = dataId;
        this.title = title;
        this.description = description;
        this.sentAt = sentAt;
        this.seen = seen;
        this.senderId = senderId;
        this.senderPictureURL = senderPictureURL;
        this.senderName = senderName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public NotificationVO(Parcel in){
        this.id = in.readInt();
        this.type = in.readInt();
        this.userId = in.readInt();
        this.dataId = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        this.sentAt = in.readString();
        this.seen = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.senderId = in.readInt();
        this.senderPictureURL = in.readString();
        this.senderName = in.readString();

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(type);
        parcel.writeInt(userId);
        parcel.writeInt(dataId);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(sentAt);
        parcel.writeValue(seen);
        parcel.writeInt(senderId);
        parcel.writeString(senderPictureURL);
        parcel.writeString(senderName);
    }

    public static final Creator<NotificationVO> CREATOR = new Creator<NotificationVO>() {

        @Override
        public NotificationVO createFromParcel(Parcel source) {
            return new NotificationVO(source);
        }

        @Override
        public NotificationVO[] newArray(int size) {
            return new NotificationVO[size];
        }
    };

    //Getters/Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderPictureURL() {
        return senderPictureURL;
    }

    public void setSenderPictureURL(String senderPictureURL) {
        this.senderPictureURL = senderPictureURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
