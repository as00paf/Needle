package com.needletest.pafoid.needletest.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class Haystack implements Serializable, Parcelable {

    private int id;
    private String name;
    private Boolean isPublic;
    private String timeLimit;
    private ArrayList<User> users;
    private ArrayList<User> activeUsers;
    private ArrayList<User> bannedUsers;
    private String pictureURL = "";
    private String zone = "";
    private int owner;

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
        this.users = new ArrayList<>();
        in.readList(this.users, getClass().getClassLoader());
        this.activeUsers = new ArrayList<>();
        in.readList(this.activeUsers, getClass().getClassLoader());
        this.bannedUsers = new ArrayList<>();
        in.readList(this.bannedUsers, getClass().getClassLoader());
        this.pictureURL = in.readString();
        this.zone = in.readString();
        this.owner = in.readInt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        StringBuffer res = new StringBuffer();
        String[] strArr = name.split(" ");
        for (String str : strArr) {
            char[] stringArray = str.trim().toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            str = new String(stringArray);

            res.append(str).append(" ");
        }

        return res.toString();
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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> value) {
        this.users = value;
    }

    public ArrayList<User> getActiveUsers() {
        if(activeUsers == null){
            activeUsers = new ArrayList<User>();
        }

        return activeUsers;
    }

    public void setActiveUsers(ArrayList<User> value) {
        this.activeUsers = value;
    }

    public ArrayList<User> getBannedUsers() {
        if(bannedUsers == null){
            bannedUsers = new ArrayList<User>();
        }
        return bannedUsers;
    }

    public void setBannedUsers(ArrayList<User> value) {
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

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
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
        parcel.writeInt(owner);
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
