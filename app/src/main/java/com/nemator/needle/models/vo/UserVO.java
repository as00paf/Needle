package com.nemator.needle.models.vo;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.utils.AppConstants;

import java.io.Serializable;

import twitter4j.User;

public class UserVO implements Serializable, Parcelable{

    @SerializedName("userId")
    private int userId = -1;

    @SerializedName("userName")
    private String userName;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("pictureURL")
    private String pictureURL;

    @SerializedName("coverPictureURL")
    private String coverPictureURL;

    @SerializedName("gcmRegId")
    private String gcmRegId;

    @SerializedName("socialNetworkUserId")
    private String socialNetworkUserId;

    @SerializedName("loginType")
    private int loginType = 0;

    public UserVO(){

    }

    public UserVO(int userId, String userName, String pictureURL, String gcmRegId){
        this.userId = userId;
        this.userName = userName;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
    }

    public UserVO(int userId, String userName, String email, String password, String pictureURL, String gcmRegId, int loginType, String socialNetworkUserId){
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
        this.loginType = loginType;
        this.socialNetworkUserId = socialNetworkUserId;
    }

    public UserVO(Parcel in){
        userId = in.readInt();
        userName = in.readString();
        email = in.readString();
        password = in.readString();
        pictureURL = in.readString();
        gcmRegId = in.readString();
        loginType = in.readInt();
        socialNetworkUserId = in.readString();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getSocialNetworkUserId() {
        return socialNetworkUserId;
    }

    public void setSocialNetworkUserId(String socialNetworkUserId) {
        this.socialNetworkUserId = socialNetworkUserId;
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
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(pictureURL);
        dest.writeString(gcmRegId);
        dest.writeInt(loginType);
        dest.writeString(socialNetworkUserId);
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

    public void save(SharedPreferences mSharedPreferences) {
        mSharedPreferences.edit()
                .putInt(AppConstants.TAG_USER_ID, userId)
                .putInt(AppConstants.TAG_LOGIN_TYPE, loginType)
                .putString(AppConstants.TAG_USER_NAME, userName)
                .putString(AppConstants.TAG_EMAIL, email)
                .putString(AppConstants.TAG_PASSWORD, password)
                .putString(AppConstants.TAG_PICTURE_URL, pictureURL)
                .putString(AppConstants.TAG_COVER_PICTURE_URL, coverPictureURL)
                .putString(AppConstants.TAG_GCM_REG_ID, gcmRegId)
                .putString(AppConstants.TAG_SOCIAL_NETWORK_USER_ID, socialNetworkUserId)
                .commit();
    }

    public static UserVO retrieve(SharedPreferences mSharedPreferences){
        UserVO vo = new UserVO();

        vo.userId = mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1);
        vo.userName = mSharedPreferences.getString(AppConstants.TAG_USER_NAME, null);
        vo.email = mSharedPreferences.getString(AppConstants.TAG_EMAIL, null);
        vo.password = mSharedPreferences.getString(AppConstants.TAG_PASSWORD, null);
        vo.pictureURL = mSharedPreferences.getString(AppConstants.TAG_PICTURE_URL, null);
        vo.coverPictureURL = mSharedPreferences.getString(AppConstants.TAG_COVER_PICTURE_URL, null);
        vo.gcmRegId = mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, null);
        vo.socialNetworkUserId = mSharedPreferences.getString(AppConstants.TAG_SOCIAL_NETWORK_USER_ID, null);
        vo.loginType = mSharedPreferences.getInt(AppConstants.TAG_LOGIN_TYPE, 0);

        return vo;
    }


}
