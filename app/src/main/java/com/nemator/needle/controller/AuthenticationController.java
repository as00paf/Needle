package com.nemator.needle.controller;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.SocialNetworkManager.OnInitializationCompleteListener;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.tasks.login.LoginTask.LoginResponseHandler;
import com.nemator.needle.tasks.login.LoginTaskParams;
import com.nemator.needle.tasks.login.LoginTaskResult;
import com.nemator.needle.tasks.user.UserTask;
import com.nemator.needle.tasks.user.UserTask.RegisterResponseHandler;
import com.nemator.needle.tasks.user.UserTaskParams;
import com.nemator.needle.tasks.user.UserTaskResult;
import com.nemator.needle.utils.AppConstants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.neokree.materialnavigationdrawer.elements.MaterialAccount;

public class AuthenticationController implements LoginResponseHandler, RegisterResponseHandler,
                                                 OnInitializationCompleteListener, OnLoginCompleteListener {

    private static final String TAG = "AuthenticationCtrl";

    public static final int LOGIN_TYPE_DEFAULT = 0;
    public static final int LOGIN_TYPE_TWITTER = 1;
    public static final int LOGIN_TYPE_LINKEDIN = 2;
    public static final int LOGIN_TYPE_GOOGLE = 3;
    public static final int LOGIN_TYPE_FACEBOOK = 4;

    public static final int LOGIN_REQUEST_TYPE_REGISTER = 0;
    public static final int LOGIN_REQUEST_TYPE_LOGIN = 1;

    private MainActivity activity;
    private UserModel userModel;
    private NavigationController navigationController;
    private SharedPreferences mSharedPreferences;
    private SocialNetworkManager mSocialNetworkManager;
    private SocialNetwork socialNetwork;
    private Fragment socialNetworkManagerFragment;
    private int loginRequestType;
    public Bitmap pictureBitmap;

    public AuthenticationController(MainActivity activity, UserModel userModel, NavigationController navigationController){
        this.activity = activity;
        this.userModel = userModel;
        this.navigationController = navigationController;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void initSocialNetworkManager(Fragment fragment) {
        ArrayList<String> fbScope = new ArrayList<String>();
        fbScope.addAll(Arrays.asList("public_profile, email, user_friends, user_location, user_birthday"));

        mSocialNetworkManager = (SocialNetworkManager) activity.getSupportFragmentManager().findFragmentByTag(NavigationController.SOCIAL_NETWORK_TAG);
        if (mSocialNetworkManager == null || socialNetworkManagerFragment != fragment) {
            socialNetworkManagerFragment = fragment;
            mSocialNetworkManager = new SocialNetworkManager();
            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(fragment, fbScope);
            mSocialNetworkManager.addSocialNetwork(fbNetwork);

            activity.getSupportFragmentManager().beginTransaction().add(mSocialNetworkManager, NavigationController.SOCIAL_NETWORK_TAG).commit();

            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        }else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if(!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    //getSocialProfile(socialNetwork.getID());
                }
            }
        }
    }

    @Override
    public void onSocialNetworkManagerInitialized() {
        if(mSocialNetworkManager != null){
            for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
                socialNetwork.setOnLoginCompleteListener(this);
                Log.i(TAG, "Social Network Initialized ! " + socialNetwork.getID());
            }
        }
    }

    //Handlers
    @Override
    public void onUserRegistered(UserTaskResult result) {
        navigationController.hideProgress();

        if(result.successCode==1){
            LoginTaskParams params = null;
            if(loginRequestType == LOGIN_REQUEST_TYPE_LOGIN){
                params = new LoginTaskParams(result.user.getUserName(), result.user.getPassword(), activity, true, false);
            }else if(loginRequestType == LOGIN_REQUEST_TYPE_REGISTER){
                params = new LoginTaskParams(result.user.getLoginType(), result.user.getUserName(), result.user.getFbId(), result.user.getGcmRegId(), activity, true, false);
            }

            if(params != null){
                new LoginTask(params, this).execute();
            }

        }else{
            Toast.makeText(activity, "Error ! \nPlease Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoginComplete(LoginTaskResult result) {
        if(!userModel.isLoggedIn()) {
            navigationController.hideProgress();

            if (result.successCode == 1) {
                userModel.setLoggedIn(true);
                userModel.setUserName(result.user.getUserName());
                userModel.setUserId(result.user.getUserId());
                userModel.getUser().setLoginType(result.user.getLoginType());
                userModel.getUser().setPictureURL(result.user.getPictureURL());
                userModel.getUser().setCoverPictureURL(result.user.getCoverPictureURL());

                setAccount();

                //Save infos
                mSharedPreferences.edit().putString(AppConstants.TAG_USER_NAME, result.user.getUserName()).
                        putInt(AppConstants.TAG_USER_ID, result.user.getUserId()).
                        putInt(AppConstants.TAG_LOGIN_TYPE, result.user.getLoginType()).
                        putString(AppConstants.TAG_GCM_REG_ID, result.user.getGcmRegId()).
                        commit();

                //Add/Remove Sections
                navigationController.removeSection(AppConstants.SECTION_LOGIN);
                navigationController.removeSection(AppConstants.SECTION_REGISTER);
                navigationController.createMainSections();
                navigationController.showSection(AppConstants.SECTION_HAYSTACKS);

                navigationController.setHaystacksCount(result.haystackCount);
                navigationController.setLocationSharingCount(result.locationSharingCount);
            }
            else if(result.successCode == 404){
                Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Public Mehtods
    public void setAccount(){
        if(activity.getAccountList().size() > 0){
            activity.removeAccount((MaterialAccount) activity.getAccountList().get(0));
        }

        String pictureURL = userModel.getUser().getPictureURL();
        Picasso.with(activity.getApplicationContext()).load(pictureURL).into(target);

        activity.addAccount(new MaterialAccount(activity.getResources(), userModel.getUserName(), "e-mail", pictureBitmap, R.drawable.mat));
        activity.setFirstAccountPhoto(activity.getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.i(TAG, "Profile pic loaded !");

            MaterialAccount account = (MaterialAccount) activity.getAccountList().get(0);
            if(activity.getAccountList().size() > 0 ){
                account.setPhoto(bitmap);
                activity.notifyAccountDataChanged();
            }else{
                pictureBitmap = bitmap;
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.e(TAG, "Could not load profile pic");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.i(TAG, "Loading profile pic ...");
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(NavigationController.SOCIAL_NETWORK_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        NavigationController.hideProgress();

        switch (socialNetworkID) {
            case LOGIN_TYPE_DEFAULT:
                Log.i(TAG, "LOGIN_TYPE_DEFAULT login success !");
                //facebook.setText("Show Facebook profile");
                break;
            case LOGIN_TYPE_FACEBOOK:
                Log.i(TAG, "LOGIN_TYPE_FACEBOOK login success !");
                if(loginRequestType == LOGIN_REQUEST_TYPE_REGISTER){
                    getSocialProfileAndRegister(socialNetworkID);
                }else if(loginRequestType == LOGIN_REQUEST_TYPE_LOGIN){
                    getSocialProfileAndLogIn(socialNetworkID);
                }
                break;
            case LOGIN_TYPE_TWITTER:
                Log.i(TAG, "LOGIN_TYPE_TWITTER login success !");
                //twitter.setText("Show Twitter profile");
                break;
            case LOGIN_TYPE_LINKEDIN:
                Log.i(TAG, "LOGIN_TYPE_LINKEDIN login success !");
                //linkedin.setText("Show LinkedIn profile");
                break;
        }
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        NavigationController.hideProgress();
        Toast.makeText(activity, "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    public void getSocialProfileAndLogIn(int networkId) {
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {
            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                switch (socialNetworkId) {
                    case LOGIN_TYPE_FACEBOOK:
                        String username = socialPerson.name;
                        String regId = userModel.getGcmRegId();
                        String fbId = socialPerson.id;

                        LoginTaskParams params = new LoginTaskParams(socialNetworkId, username, fbId, regId, activity, true, false);
                        new LoginTask(params, AuthenticationController.this).execute();
                        break;
                }

            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {

            }
        });
        socialNetwork.requestCurrentPerson();
    }

    public void getSocialProfileAndRegister(int networkId) {
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {
            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                switch (socialNetworkId) {
                    case LOGIN_TYPE_FACEBOOK:
                        String username = socialPerson.name;
                        String regId = userModel.getGcmRegId();
                        String fbId = socialPerson.id;

                        UserVO userVO = new UserVO(-1, username, null, regId);
                        userVO.setLoginType(socialNetworkId);
                        userVO.setFbId(fbId);
                        userVO.setPictureURL(socialPerson.avatarURL);
                        userVO.setPictureURL(socialPerson.profileURL);

                        UserTaskParams params = new UserTaskParams(activity, UserTaskParams.TYPE_REGISTER, userVO);
                        new UserTask(params, AuthenticationController.this).execute();
                        break;
                }
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {

            }
        });
        socialNetwork.requestCurrentPerson();
    }

    public void logInWithSocialNetwork(int networkId) {
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        if(!socialNetwork.isConnected()) {
            if(networkId != 0) {
                Log.i(TAG, "Requesting login on network with id : !" + networkId);
                loginRequestType = LOGIN_REQUEST_TYPE_LOGIN;
                socialNetwork.requestLogin(this);
                NavigationController.showProgress(activity.getString(R.string.login_message));
            } else {
                Toast.makeText(activity, "Wrong networkId", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Social Network Connected !");
            NavigationController.showProgress(activity.getString(R.string.login_message));
            getSocialProfileAndLogIn(networkId);
        }
    }

    public void logOut() {
        socialNetwork.logout();
    }

    public void registerWithNetwork(int networkId) {
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        if(!socialNetwork.isConnected()) {
            if(networkId != 0) {
                Log.i(TAG, "Requesting login on network with id : !" + networkId);
                loginRequestType = LOGIN_REQUEST_TYPE_REGISTER;
                socialNetwork.requestLogin(this);
                NavigationController.showProgress(activity.getString(R.string.registering_message));
            } else {
                Toast.makeText(activity, "Wrong networkId", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Social Network Connected !");
            NavigationController.showProgress(activity.getString(R.string.registering_message));
            getSocialProfileAndRegister(networkId);
        }
    }
}
