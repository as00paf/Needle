package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserVO implements Serializable, Parcelable{

    private int userId = -1;
    private String userName;
    private String password;
    private String pictureURL;
    private String coverPictureURL;

    private String gcmRegId;
    private String fbId;
    private String twitterId;
    private String googleId;
    private int loginType = 0;

    public UserVO(){

    }

    public UserVO(int userId, String userName, String pictureURL, String gcmRegId){
        this.userId = userId;
        this.userName = userName;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
    }

    public UserVO(int userId, String userName, String password, String pictureURL, String gcmRegId, int loginType){
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
        this.loginType = loginType;
    }

    public UserVO(Parcel in){
        userId = in.readInt();
        userName = in.readString();
        password = in.readString();
        pictureURL = in.readString();
        gcmRegId = in.readString();
        loginType = in.readInt();
        fbId = in.readString();
        twitterId = in.readString();
        googleId = in.readString();
        coverPictureURL = in.readString();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getGcmRegId() {
        return gcmRegId;
    }

    public void setGcmRegId(String gcmRegId) {
        this.gcmRegId = gcmRegId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getCoverPictureURL() {
        return coverPictureURL;
    }

    public void setCoverPictureURL(String coverPictureURL) {
        this.coverPictureURL = coverPictureURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeString(password);
        dest.writeString(pictureURL);
        dest.writeString(gcmRegId);
        dest.writeInt(loginType);
        dest.writeString(fbId);
        dest.writeString(twitterId);
        dest.writeString(googleId);
        dest.writeString(coverPictureURL);
    }

    public static final Parcelable.Creator<UserVO> CREATOR = new Parcelable.Creator<UserVO>() {

        @Override
        public UserVO createFromParcel(Parcel source) {
            return new UserVO(source);
        }

        @Override
        public UserVO[] newArray(int size) {
            return new UserVO[size];
        }
    };
}
