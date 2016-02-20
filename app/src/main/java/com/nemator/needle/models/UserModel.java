package com.nemator.needle.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.FacebookUserVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

public class UserModel {

    private static UserModel instance;

    private boolean loggedIn = false;
    private boolean autoLogin = true;

    private UserVO user;
    private SharedPreferences sharedPreferences;

    public UserModel(){
    }

    public static UserModel getInstance(){
        if(instance == null){
            instance = new UserModel();
        }

        return instance;
    }

    public void init(Context context){
        sharedPreferences = context.getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE);
        user = UserVO.retrieve(sharedPreferences);
        loggedIn =  sharedPreferences.getBoolean(AppConstants.TAG_LOGGED_IN, false);
    }

    //Getters/Setters
    public UserVO getUser(){
        return this.user;
    }

    public void setUser(UserVO user) {
        this.user = user;
        saveUser();
    }

    public void clearUser() {
        this.user = null;
        saveUser();
    }

    public int getUserId(){
        return user.getUserId();
    }

    public void setUserId(int userId){
        user.setUserId(userId);
        sharedPreferences.edit().putInt(AppConstants.TAG_USER_ID, userId).commit();
    }

    public String getUserName(){
        return user.getUserName();
    }

    public void setUserName(String username){
        user.setUserName(username);

        sharedPreferences.edit().putString(AppConstants.TAG_USER_NAME, username).commit();
    }

    public String getGcmRegId() {
        return user.getGcmRegId();
    }

    public Boolean setGcmRegId(String gcmRegId) {
        Boolean wereTheSame = user.getGcmRegId() != null && !user.getGcmRegId().equals(gcmRegId);
        user.setGcmRegId(gcmRegId);
        sharedPreferences.edit().putString(AppConstants.TAG_GCM_REG_ID, gcmRegId).apply();

        return wereTheSame;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        sharedPreferences.edit().putBoolean(AppConstants.TAG_LOGGED_IN, loggedIn).apply();
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public void saveUser(){
        user.save(sharedPreferences);
    }

    public void setUserFromFacebookAccount(FacebookUserVO fbUser) {
        this.user.setEmail(fbUser.getEmail());
        this.user.setPictureURL(fbUser.getPicture().getData().getUrl());
        this.user.setCoverPictureURL(fbUser.getCover().getSource());
        this.user.setUserName(fbUser.getName());
        this.user.setSocialNetworkUserId(fbUser.getId());
        this.user.setLoginType(AuthenticationController.LOGIN_TYPE_FACEBOOK);
    }
}
