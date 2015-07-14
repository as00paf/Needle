package com.nemator.needle.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemator.needle.utils.AppConstants;

public class UserModel {

    private final Context context;
    private SharedPreferences mSharedPreferences;

    private int userId = -1;
    private String userName;
    private String gcmRegId;
    private boolean loggedIn = false;
    private boolean autoLogin = true;


    public UserModel(Context context){
        this.context = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        userName = mSharedPreferences.getString(AppConstants.TAG_USER_NAME, "");
        gcmRegId = mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, "");
        userId = mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1);
    }

    //Getters/Setters
    public int getUserId(){
        return userId;
    }

    public String getUserName(){
        return userName;
    }

    public String getGcmRegId() {
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
