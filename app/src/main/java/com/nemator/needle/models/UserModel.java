package com.nemator.needle.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

public class UserModel {

    private final Context context;
    private SharedPreferences mSharedPreferences;

    private boolean loggedIn = false;
    private boolean autoLogin = true;

    private UserVO user;


    public UserModel(Context context){
        this.context = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userName = mSharedPreferences.getString(AppConstants.TAG_USER_NAME, "");
        String gcmRegId = mSharedPreferences.getString(AppConstants.TAG_GCM_REG_ID, "");
        int userId = mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1);

        user = new UserVO(userId, userName, null, gcmRegId);
    }

    //Getters/Setters
    public UserVO getUser(){
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public int getUserId(){
        if(user.getUserId() == -1)
            user.setUserId(mSharedPreferences.getInt(AppConstants.TAG_USER_ID, -1));
        return user.getUserId();
    }

    public void setUserId(int userId){
        user.setUserId(userId);
        mSharedPreferences.edit().putInt(AppConstants.TAG_USER_ID, userId);
    }

    public String getUserName(){
        return user.getUserName();
    }

    public void setUserName(String username){
        user.setUserName(username);

        mSharedPreferences.edit().putString(AppConstants.TAG_USER_NAME, username);
    }

    public String getGcmRegId() {
        return user.getGcmRegId();
    }

    public Boolean setGcmRegId(String gcmRegId) {
        Boolean wereTheSame = user.getGcmRegId().equals(gcmRegId);
        user.setGcmRegId(gcmRegId);
        mSharedPreferences.edit().putString(AppConstants.TAG_GCM_REG_ID, gcmRegId);

        return wereTheSame;
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
