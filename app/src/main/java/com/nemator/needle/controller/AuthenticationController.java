package com.nemator.needle.controller;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.SocialNetworkManager.OnInitializationCompleteListener;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.googleplus.GooglePlusSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthenticationController implements LoginResponseHandler, RegisterResponseHandler, OnLoginCompleteListener {

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
    public Bitmap coverPictureBitmap;

    public AuthenticationController(MainActivity activity, UserModel userModel, NavigationController navigationController){
        this.activity = activity;
        this.userModel = userModel;
        this.navigationController = navigationController;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void initSocialNetworkManager(Fragment fragment, final Boolean logIn) {
        ArrayList<String> fbScope = new ArrayList<String>();
        fbScope.addAll(Arrays.asList("public_profile, email, user_friends, user_location, user_birthday"));

        mSocialNetworkManager = (SocialNetworkManager) activity.getSupportFragmentManager().findFragmentByTag(NavigationController.SOCIAL_NETWORK_TAG);
        if (mSocialNetworkManager == null || socialNetworkManagerFragment != fragment) {
            socialNetworkManagerFragment = fragment;
            mSocialNetworkManager = new SocialNetworkManager();

            //Facebook
            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(fragment, fbScope);
            mSocialNetworkManager.addSocialNetwork(fbNetwork);

            //Twitter
            String key = AppConstants.TWITTER_API_KEY;
            String secret = AppConstants.TWITTER_API_SECRET;

            TwitterSocialNetwork twitterNetwork = new TwitterSocialNetwork(fragment, key, secret, "x-oauthflow-twitter://callback");
            mSocialNetworkManager.addSocialNetwork(twitterNetwork);

            //Google
            GooglePlusSocialNetwork googleNetwork = new GooglePlusSocialNetwork(fragment);
            mSocialNetworkManager.addSocialNetwork(googleNetwork);

            activity.getSupportFragmentManager().beginTransaction().add(mSocialNetworkManager, NavigationController.SOCIAL_NETWORK_TAG).commit();

            mSocialNetworkManager.setOnInitializationCompleteListener(new OnInitializationCompleteListener() {
                @Override
                public void onSocialNetworkManagerInitialized() {
                    if(mSocialNetworkManager != null && socialNetwork == null){
                        List<SocialNetwork> initializedNetworks =  mSocialNetworkManager.getInitializedSocialNetworks();
                        for (SocialNetwork socialNetwork : initializedNetworks) {
                            socialNetwork.setOnLoginCompleteListener(AuthenticationController.this);
                            Log.i(TAG, "Social Network Initialized ! " + socialNetwork.getID());
                        }

                        if(logIn){
                            //Login with social network favorising FB
                            int networkId = LOGIN_TYPE_FACEBOOK;
                            int count = initializedNetworks.size();
                            if(count > 0){
                                if(count == 1 || !initializedNetworks.contains(LOGIN_TYPE_FACEBOOK)){
                                    networkId = initializedNetworks.get(0).getID();
                                }

                                logInWithSocialNetwork(networkId);
                            }
                        }
                    }
                }
            });
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

    //Handlers
    @Override
    public void onUserRegistered(UserTaskResult result) {
        navigationController.hideProgress();

        if(result.successCode==1){
            WeakReference<TextView> textView = new WeakReference<TextView>((TextView) activity.findViewById(R.id.login_splash_label));
            LoginTaskParams params = new LoginTaskParams(activity, result.user, textView);
            new LoginTask(params, this).execute();
        }else{
            Toast.makeText(activity, "Error ! \nPlease Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoginComplete(LoginTaskResult result) {
        navigationController.hideProgress();

        if(!userModel.isLoggedIn()) {
            if (result.successCode == 1) {
                userModel.setLoggedIn(true);
                userModel.setUserName(result.user.getUserName());
                userModel.setUserId(result.user.getUserId());
                userModel.getUser().setSocialNetworkUserId(result.user.getSocialNetworkUserId());
                userModel.getUser().setLoginType(result.user.getLoginType());
                userModel.getUser().setPictureURL(result.user.getPictureURL());
                userModel.getUser().setCoverPictureURL(result.user.getCoverPictureURL());
                userModel.saveUser();

                navigationController.setAccount();

                navigationController.createMainSections();
                navigationController.showSection(AppConstants.SECTION_HAYSTACKS);

                navigationController.setHaystacksCount(result.haystackCount);
                navigationController.setLocationSharingCount(result.locationSharingCount);
            }
            else if(result.successCode == 404){
                Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                //navigationController.removeLoginSplash();
                //navigationController.showSection(AppConstants.SECTION_REGISTER);
                getSocialProfileAndRegister(result.type);
            }
            else {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Log.i(TAG, "here");
        }
    }

    //Public Mehtods
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(NavigationController.SOCIAL_NETWORK_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
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
            Log.i(TAG, "Social Network Connected ! " + networkId);
            NavigationController.showProgress(activity.getString(R.string.login_message));
            getSocialProfileAndLogIn(networkId);
        }
    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        NavigationController.hideProgress();

        switch (socialNetworkID) {
            case LOGIN_TYPE_FACEBOOK:
                Log.i(TAG, "LOGIN_TYPE_FACEBOOK login success !");
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

        if(loginRequestType == LOGIN_REQUEST_TYPE_REGISTER){
            getSocialProfileAndRegister(socialNetworkID);
        }else if(loginRequestType == LOGIN_REQUEST_TYPE_LOGIN){
            getSocialProfileAndLogIn(socialNetworkID);
        }
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        NavigationController.hideProgress();
        //Toast.makeText(activity, "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, errorMessage);

        if(requestID == SocialNetwork.REQUEST_LOGIN){
            navigationController.hideProgress();
            navigationController.removeLoginSplash();
            navigationController.showSection(AppConstants.SECTION_REGISTER);
        }
    }

    public void getSocialProfileAndLogIn(final int networkId) {
        Log.i(TAG, "getSocialProfileAndLogIn");
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {

            private int tryCount = 1;

            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                socialNetwork.setOnRequestRemoveFriendCompleteListener(null);

                LoginTaskParams params;
                String username = socialPerson.name;
                String regId = userModel.getGcmRegId();
                String id = socialPerson.id;

                UserVO user = new UserVO(-1, username, "", "", regId, socialNetworkId, id);

                WeakReference<TextView> textView = new WeakReference<TextView>((TextView) activity.findViewById(R.id.login_splash_label));
                params = new LoginTaskParams(activity, user, textView);
                new LoginTask(params, AuthenticationController.this).execute();
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
                Log.e(TAG, "getSocialProfileAndLogIn( " + networkId + " ) :: Error : " + errorMessage);
                //authorize app

                tryCount++;
            }
        });
        socialNetwork.requestCurrentPerson();
    }

    public void getSocialProfileAndRegister(final int networkId) {
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {
            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                switch (socialNetworkId) {
                    case LOGIN_TYPE_FACEBOOK:
                        Log.i(TAG, "Succesfuly retreived social profile of type FACEBOOK");
                        break;
                    case LOGIN_TYPE_TWITTER:
                        Log.i(TAG, "Succesfuly retreived social profile of type TWITTER");
                        break;
                    case LOGIN_TYPE_GOOGLE:
                           Log.i(TAG, "Succesfuly retreived social profile of type GOOGLE+");
                        break;
                }

                String username = socialPerson.name;
                String regId = userModel.getGcmRegId();
                String id = socialPerson.id;

                UserVO userVO = new UserVO(-1, username, null, regId);
                userVO.setLoginType(socialNetworkId);
                userVO.setSocialNetworkUserId(id);
                userVO.setPictureURL(socialPerson.avatarURL);
                userVO.setCoverPictureURL(socialPerson.profileURL);

                UserTaskParams params = new UserTaskParams(activity.getApplicationContext(), UserTaskParams.TYPE_REGISTER, userVO);
                new UserTask(params, AuthenticationController.this).execute();
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
                Log.e(TAG, "getSocialProfileAndRegister(" + networkId + ") :: Error : " + errorMessage);
            }
        });
        socialNetwork.requestCurrentPerson();
    }

    public void logOut() {
        if(socialNetwork != null){
            socialNetwork.logout();
        }
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
