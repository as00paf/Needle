package com.nemator.needle.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemator.needle.Needle;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

public class UserModel {

    private static UserModel instance;

    private SharedPreferences mSharedPreferences;

    private boolean loggedIn = false;
    private boolean autoLogin = true;

    private UserVO user;

    public UserModel(){
    }

    public static UserModel getInstance(){
        if(instance == null){
            instance = new UserModel();
        }

        return instance;
    }

    public void init(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        user = UserVO.retrieve(mSharedPreferences);
        loggedIn =  mSharedPreferences.getBoolean(AppConstants.TAG_LOGGED_IN, false);
    }

    //Getters/Setters
    public UserVO getUser(){
        return this.user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public int getUserId(){
        return user.getUserId();
    }

    public void setUserId(int userId){
        user.setUserId(userId);
        mSharedPreferences.edit().putInt(AppConstants.TAG_USER_ID, userId).commit();
    }

    public String getUserName(){
        return user.getUserName();
    }

    public void setUserName(String username){
        user.setUserName(username);

        mSharedPreferences.edit().putString(AppConstants.TAG_USER_NAME, username).commit();
    }

    public String getGcmRegId() {
        return user.getGcmRegId();
    }

    public Boolean setGcmRegId(String gcmRegId) {
        Boolean wereTheSame = user.getGcmRegId() != null && !user.getGcmRegId().equals(gcmRegId);
        user.setGcmRegId(gcmRegId);
        mSharedPreferences.edit().putString(AppConstants.TAG_GCM_REG_ID, gcmRegId).commit();

        return wereTheSame;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        mSharedPreferences.edit().putBoolean(AppConstants.TAG_LOGGED_IN, loggedIn).commit();
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public void saveUser(){
        user.save(mSharedPreferences);
    }
}
