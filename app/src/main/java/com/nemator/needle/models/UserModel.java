package com.nemator.needle.models;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemator.needle.MainActivity;
import com.nemator.needle.utils.AppConstants;

public class UserModel {

    private final MainActivity activity;
    private SharedPreferences mSharedPreferences;

    private int userId = -1;
    private String userName;
    private String gcmRegId;
    private boolean loggedIn = false;
    private boolean autoLogin = true;


    public UserModel(MainActivity activity){
        this.activity = activity;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    //Getters/Setters
    public int getUserId(){
        if(userId==-1){
            userId = mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1);
        }

        return userId;
    }

    public String getUserName(){
        if(userName == null || userName.isEmpty()){
            userName = mSharedPreferences.getString(AppConstants.TAG_USER_NAME, "");
        }

        return userName;
    }

    public String getGcmRegId() {
        if(gcmRegId == null || gcmRegId.isEmpty()){
            gcmRegId = mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, "");
        }

        return gcmRegId;
    }

    public void setGcmRegId(String gcmRegId) {
        this.gcmRegId = gcmRegId;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }
}
