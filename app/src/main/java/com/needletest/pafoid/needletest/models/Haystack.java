package com.needletest.pafoid.needletest.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Haystack implements Serializable, Parcelable {

    private int id;
    private String name;
    private Boolean isPublic;
    private String timeLimit;
    private ArrayList<String> users;
    private ArrayList<String> activeUsers;
    private ArrayList<String> bannedUsers;
    private String pictureURL = "";
    private String zone = "";
    private String owner;

    public Haystack(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Haystack(Parcel in){
        this.id = in.readInt();
        this.name = in.readString();
        this.isPublic = (Boolean) in.readValue(ClassLoader.getSystemClassLoader());
        this.timeLimit = in.readString();
        in.readList(this.users, getClass().getClassLoader());
        in.readList(this.activeUsers, getClass().getClassLoader());
        in.readList(this.bannedUsers, getClass().getClassLoader());
        this.pictureURL = in.readString();
        this.zone = in.readString();
        this.owner = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> value) {
        this.users = value;
    }

    public ArrayList<String> getActiveUsers() {
        if(activeUsers == null){
            activeUsers = new ArrayList<String>();
        }

        return activeUsers;
    }

    public void setActiveUsers(ArrayList<String> value) {
        this.activeUsers = value;
    }

    public ArrayList<String> getBannedUsers() {
        if(bannedUsers == null){
            bannedUsers = new ArrayList<String>();
        }
        return bannedUsers;
    }

    public void setBannedUsers(ArrayList<String> value) {
        this.bannedUsers = value;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeValue(isPublic);
        parcel.writeString(timeLimit);
        parcel.writeList(users);
        parcel.writeList(activeUsers);
        parcel.writeList(bannedUsers);
        parcel.writeString(pictureURL);
        parcel.writeString(zone);
        parcel.writeString(owner);
    }

    public static final Parcelable.Creator<Haystack> CREATOR = new Parcelable.Creator<Haystack>() {

        @Override
        public Haystack createFromParcel(Parcel source) {
            return new Haystack(source);
        }

        @Override
        public Haystack[] newArray(int size) {
            return new Haystack[size];
        }
    };
}
