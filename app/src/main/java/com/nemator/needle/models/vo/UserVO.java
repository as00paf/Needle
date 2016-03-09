package com.nemator.needle.models.vo;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.utils.AppConstants;

import java.io.Serializable;

public class UserVO implements Serializable, Parcelable{

    @SerializedName("id")
    private int id = -1;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String userName;

    @SerializedName("password")
    private String password;

    @SerializedName("gcmRegId")
    private String gcmRegId;

    @SerializedName("pictureURL")
    private String pictureURL="";

    @SerializedName("coverPictureURL")
    private String coverPictureURL="";

    @SerializedName("socialNetworkUserId")
    private String socialNetworkUserId;

    @SerializedName("loginType")
    private int loginType = 0;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("location")
    private LocationVO location;

    /**
     *
     */
    public UserVO(){

    }

    /**
     * @param id
     * @param userName
     * @param pictureURL
     * @param gcmRegId
     */
    public UserVO(int id, String userName, String pictureURL, String gcmRegId){
        this.id = id;
        this.userName = userName;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
    }

    /**
     * @param id id of user
     * @param userName username
     * @param email email
     * @param password password
     * @param pictureURL picture url
     * @param gcmRegId Google Registration Id
     * @param loginType login type
     * @param socialNetworkUserId Facebook/Google/Twitter id
     * @param isActive
     * @param location
     */
    public UserVO(int id, String userName, String email, String password, String pictureURL, String gcmRegId, int loginType, String socialNetworkUserId, Boolean isActive, LocationVO location){
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.pictureURL = pictureURL;
        this.gcmRegId = gcmRegId;
        this.loginType = loginType;
        this.socialNetworkUserId = socialNetworkUserId;
        this.isActive = isActive;
        this.location = location;
    }

    /**
     * @param in
     */
    public UserVO(Parcel in){
        id = in.readInt();
        userName = in.readString();
        email = in.readString();
        password = in.readString();
        pictureURL = in.readString();
        gcmRegId = in.readString();
        loginType = in.readInt();
        socialNetworkUserId = in.readString();
        coverPictureURL = in.readString();
        isActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public int getId() {
        return id;
    }

    public String getStringId() {
        return String.valueOf(id);
    }

    public UserVO setId(int id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserVO setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserVO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public UserVO setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
        return this;
    }

    public String getGcmRegId() {
        return gcmRegId;
    }

    public UserVO setGcmRegId(String gcmRegId) {
        this.gcmRegId = gcmRegId;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserVO setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getSocialNetworkUserId() {
        return socialNetworkUserId;
    }

    public UserVO setSocialNetworkUserId(String socialNetworkUserId) {
        this.socialNetworkUserId = socialNetworkUserId;
        return this;
    }

    public int getLoginType() {
        return loginType;
    }

    public UserVO setLoginType(int loginType) {
        this.loginType = loginType;
        return this;
    }

    public String getCoverPictureURL() {
        return coverPictureURL;
    }

    public UserVO setCoverPictureURL(String coverPictureURL) {
        this.coverPictureURL = coverPictureURL;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(pictureURL);
        dest.writeString(gcmRegId);
        dest.writeInt(loginType);
        dest.writeString(socialNetworkUserId);
        dest.writeString(coverPictureURL);
        dest.writeValue(isActive);
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
                .putInt(AppConstants.TAG_USER_ID, id)
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

        vo.id = mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1);
        vo.userName = mSharedPreferences.getString(AppConstants.TAG_USER_NAME, null);
        vo.email = mSharedPreferences.getString(AppConstants.TAG_EMAIL, null);
        vo.password = mSharedPreferences.getString(AppConstants.TAG_PASSWORD, null);
        vo.pictureURL = mSharedPreferences.getString(AppConstants.TAG_PICTURE_URL, "");
        vo.coverPictureURL = mSharedPreferences.getString(AppConstants.TAG_COVER_PICTURE_URL, "");
        vo.gcmRegId = mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, null);
        vo.socialNetworkUserId = mSharedPreferences.getString(AppConstants.TAG_SOCIAL_NETWORK_USER_ID, null);
        vo.loginType = mSharedPreferences.getInt(AppConstants.TAG_LOGIN_TYPE, 0);

        return vo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public static Creator<UserVO> getCREATOR() {
        return CREATOR;
    }

    public LocationVO getLocation() {
        return location;
    }

    public void setLocation(LocationVO location) {
        this.location = location;
    }

    public String getReadableUserName() {
        String name = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        return name;
    }
}
