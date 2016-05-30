package com.nemator.needle.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.nemator.needle.Needle;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.FacebookUserVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;

public class UserModel {

    private static UserModel instance;

    private boolean loggedIn = false;
    private boolean initialized = false;

    private UserVO user;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<UserVO> friends;

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
        editor = sharedPreferences.edit();
        user = UserVO.retrieve(sharedPreferences);
        loggedIn = sharedPreferences.getBoolean(AppConstants.TAG_LOGGED_IN, false);
        initialized = true;
    }

    //Getters/Setters
    public UserVO getUser(){
        return this.user;
    }

    public UserVO getUser(Context context){
        if(this.user == null){
            this.user =  UserVO.retrieve(context.getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE));
        }
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
        return user.getId();
    }

    public void setUserId(int userId){
        user.setId(userId);
        editor.putInt(AppConstants.TAG_USER_ID, userId).commit();
    }

    public String getUserName(){
        return user.getReadableUserName();
    }

    public void setUserName(String username){
        user.setUserName(username);

        editor.putString(AppConstants.TAG_USER_NAME, username).commit();
    }

    public String getGcmRegId() {
        return user.getGcmRegId();
    }

    public String getGcmRegId(Context context) {
        return getUser(context).getGcmRegId();
    }

    public Boolean setGcmRegId(String gcmRegId) {
        Boolean wereTheSame = user.getGcmRegId() != null && !user.getGcmRegId().equals(gcmRegId);
        user.setGcmRegId(gcmRegId);
        editor.putString(AppConstants.TAG_GCM_REG_ID, gcmRegId).apply();

        return wereTheSame;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        editor.putBoolean(AppConstants.TAG_LOGGED_IN, loggedIn).apply();
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

    public boolean isInitialized() {
        return initialized;
    }

    public ArrayList<UserVO> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<UserVO> friends) {
        this.friends = friends;
    }
}
