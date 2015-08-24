package com.nemator.needle.models.vo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class HaystackVO implements Serializable, Parcelable {
    private int id;
    private int owner;
    private String name;
    private Boolean isPublic;
    private String timeLimit;

    private int zoneRadius;
    private Boolean isCircle;
    private LatLng position;
    private String pictureURL = "";
    private Bitmap picture;

    private ArrayList<UserVO> users;
    private ArrayList<UserVO> activeUsers;
    private ArrayList<UserVO> bannedUsers;

    public HaystackVO(){

    }

    public HaystackVO(int id, int owner, String name, Boolean isPublic, String timeLimit, int zoneRadius, Boolean isCircle, LatLng position, String pictureURL, ArrayList<UserVO> users, ArrayList<UserVO> activeUsers){
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.timeLimit = timeLimit;
        this.zoneRadius = zoneRadius;
        this.isCircle = isCircle;
        this.isPublic = isPublic;
        this.position = position;
        this.pictureURL = pictureURL;
        this.users = users;
        this.activeUsers = activeUsers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public HaystackVO(Parcel in){
        this.id = in.readInt();
        this.name = in.readString();
        this.owner = in.readInt();
        this.isPublic = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.timeLimit = in.readString();
        this.zoneRadius = in.readInt();
        this.isCircle = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.pictureURL = in.readString();
        this.position = new LatLng(in.readDouble(), in.readDouble());

        try{
            this.users = new ArrayList<UserVO>();
            in.readList(this.users, UserVO.class.getClassLoader());
            this.activeUsers = new ArrayList<UserVO>();
            in.readList(this.activeUsers, UserVO.class.getClassLoader());
            this.bannedUsers = new ArrayList<UserVO>();
            in.readList(this.bannedUsers, UserVO.class.getClassLoader());
        }catch(Exception e){

        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(owner);
        parcel.writeValue(isPublic);
        parcel.writeString(timeLimit);
        parcel.writeInt(zoneRadius);
        parcel.writeValue(isCircle);
        parcel.writeDouble(position.latitude);
        parcel.writeDouble(position.longitude);
        parcel.writeString(pictureURL);

        if(users == null) users = new ArrayList<UserVO>();
        parcel.writeList(users);
        if(activeUsers == null) users = new ArrayList<UserVO>();
        parcel.writeList(activeUsers);
        if(bannedUsers == null) users = new ArrayList<UserVO>();
        parcel.writeList(bannedUsers);
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

    public ArrayList<UserVO> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserVO> value) {
        this.users = value;
    }

    public ArrayList<UserVO> getActiveUsers() {
        if(activeUsers == null){
            activeUsers = new ArrayList<UserVO>();
        }

        return activeUsers;
    }

    public void setActiveUsers(ArrayList<UserVO> value) {
        this.activeUsers = value;
    }

    public ArrayList<UserVO> getBannedUsers() {
        if(bannedUsers == null){
            bannedUsers = new ArrayList<UserVO>();
        }
        return bannedUsers;
    }

    public void setBannedUsers(ArrayList<UserVO> value) {
        this.bannedUsers = value;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getZoneRadius() {
        return zoneRadius;
    }

    public void setZoneRadius(int zoneRadius) {
        this.zoneRadius = zoneRadius;
    }

    public Boolean getIsCircle() {
        return isCircle;
    }

    public void setIsCircle(Boolean isCircle) {
        this.isCircle = isCircle;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public static final Parcelable.Creator<HaystackVO> CREATOR = new Parcelable.Creator<HaystackVO>() {

        @Override
        public HaystackVO createFromParcel(Parcel source) {
            return new HaystackVO(source);
        }

        @Override
        public HaystackVO[] newArray(int size) {
            return new HaystackVO[size];
        }
    };
}
