package com.nemator.needle.controller;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.UserRegistrationResult;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.facebook.GetFacebookCoverURLTask;
import com.nemator.needle.tasks.login.LoginTask.LoginResponseHandler;
import com.nemator.needle.tasks.login.LoginTaskResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AuthenticationController implements LoginResponseHandler, OnLoginCompleteListener {

    private static final String TAG = "AuthenticationCtrl";
    private static AuthenticationController instance;

    public static final int LOGIN_TYPE_DEFAULT = 0;
    public static final int LOGIN_TYPE_TWITTER = 1;
    public static final int LOGIN_TYPE_LINKEDIN = 2;
    public static final int LOGIN_TYPE_GOOGLE = 3;
    public static final int LOGIN_TYPE_FACEBOOK = 4;

    public static final int LOGIN_REQUEST_TYPE_REGISTER = 0;
    public static final int LOGIN_REQUEST_TYPE_LOGIN = 1;

    private MainActivity activity;
    private SocialNetworkManager mSocialNetworkManager;
    private SocialNetwork socialNetwork;
    private Fragment socialNetworkManagerFragment;
    private int loginRequestType;
    public Bitmap pictureBitmap;
    public Bitmap coverPictureBitmap;
    private Handler handler = new Handler();

    private ApiClient apiClient;

    public AuthenticationController(){
        apiClient = ApiClient.getInstance();
    }

    public static AuthenticationController getInstance() {
        if(instance == null){
            instance = new AuthenticationController();
        }

        return instance;
    }

    public void init(MainActivity activity){
        this.activity = activity;
    }

    public void initSocialNetworkManager(Fragment fragment) {
        ArrayList<String> fbScope = new ArrayList<String>();
        fbScope.addAll(Arrays.asList("public_profile, email, user_friends"));

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
                    }

                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.SOCIAL_NETWORKS_INITIALIZED);
                    broadcastManager.sendBroadcast(intent);
                }
            });
        }else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if(!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                }
            }
        }
    }

    //Handlers
    @Override
    public void onLoginComplete(LoginTaskResult result) {
        if(!Needle.userModel.isLoggedIn()) {
            if (result.getSuccessCode() == 1) {
                Needle.userModel.setLoggedIn(true);
                Needle.userModel.setUserName(result.getUser().getUserName());
                Needle.userModel.getUser().setEmail(result.getUser().getEmail());
                Needle.userModel.setUserId(result.getUser().getUserId());
                Needle.userModel.getUser().setSocialNetworkUserId(result.getUser().getSocialNetworkUserId());
                Needle.userModel.getUser().setLoginType(result.getUser().getLoginType());
                Needle.userModel.getUser().setPictureURL(result.getUser().getPictureURL());
                Needle.userModel.getUser().setCoverPictureURL(result.getUser().getCoverPictureURL());
                Needle.userModel.saveUser();

                Needle.navigationController.onPostLogin();

                Needle.navigationController.setHaystacksCount(result.getHaystackCount());
                Needle.navigationController.setLocationSharingCount(result.getLocationSharingCount());
            }
            else if(result.getSuccessCode() == 404){
                Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                //getSocialProfileAndRegister(result.type);
            }
            else
            {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Log.i(TAG, "here");

            if(result.getSuccessCode() == 404){
                Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                getSocialProfileAndRegister(result.getType());
            }
        }
    }

    //Public Mehtods
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(NavigationController.SOCIAL_NETWORK_TAG);
        if (fragment != null && hasInitializedSocialNetworks()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        Log.d(TAG, "onActivityResult");

        if(requestCode == 9000 && resultCode == -1){
            PermissionManager.getInstance(activity).checkAccountsPermission(activity);
        }
    }

    private BroadcastReceiver googleApiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "googleApiConnectedReceiver:: Received intent");

            LocalBroadcastManager.getInstance(activity).unregisterReceiver(this);

            ((GooglePlusSocialNetwork) socialNetwork).setGoogleApiClient(Needle.googleApiController.getGoogleApiClient());
            ((GooglePlusSocialNetwork) socialNetwork).setConnectionResult(Needle.googleApiController.getConnectionResult());

            if(PermissionManager.getInstance(activity).checkAccountsPermission(activity)){
                getSocialProfileAndRegister(LOGIN_TYPE_GOOGLE);
            }
        }
    };

    public void logInWithSocialNetwork(int networkId) {
        Log.d(TAG, "logInWithSocialNetwork");

        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        if(!socialNetwork.isConnected()) {
            if(networkId != 0) {
                Log.i(TAG, "Requesting login on network with id : !" + networkId);
                loginRequestType = LOGIN_REQUEST_TYPE_LOGIN;

                if(socialNetwork.getID() == LOGIN_TYPE_GOOGLE){
                    if(Needle.googleApiController.isConnected()){
                        Log.i(TAG, "Api is Connected");
                        ((GooglePlusSocialNetwork) socialNetwork).setGoogleApiClient(Needle.googleApiController.getGoogleApiClient());
                        ((GooglePlusSocialNetwork) socialNetwork).setConnectionResult(Needle.googleApiController.getConnectionResult());

                        getSocialProfileAndLogIn(LOGIN_TYPE_GOOGLE);
                    }else{
                        Log.i(TAG, "Api is not Connected");

                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
                        localBroadcastManager.registerReceiver(googleApiConnectedReceiver, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));

                        Needle.googleApiController.init(activity, true);
                    }
                }else{
                    socialNetwork.requestLogin(this);
                }

                Needle.navigationController.updateSplashLabel(activity.getString(R.string.login_message));
            } else {
                Toast.makeText(activity, "Wrong networkId", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Social Network Connected ! " + networkId);
            Needle.navigationController.updateSplashLabel(activity.getString(R.string.login_message));
            getSocialProfileAndLogIn(networkId);
        }
    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        if(loginRequestType == LOGIN_REQUEST_TYPE_REGISTER){
            getSocialProfileAndRegister(socialNetworkID);
        }else if(loginRequestType == LOGIN_REQUEST_TYPE_LOGIN){
            getSocialProfileAndLogIn(socialNetworkID);
        }
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        Needle.navigationController.hideProgress();
        //Toast.makeText(activity, "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error : " + errorMessage);

        if(requestID == SocialNetwork.REQUEST_LOGIN){
            if(errorMessage == null && data == null){//FB
                return;
            }

            if(errorMessage.equals("incorrect URI returned: null")){//Twitter
                return;
            }
        }
    }

    public void getSocialProfileAndLogIn(final int networkId) {
        Log.i(TAG, "getSocialProfileAndLogIn");
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {

            private int tryCount = 1;

            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                Log.d(TAG, "onRequestSocialPersonSuccess");

                socialNetwork.setOnRequestCurrentPersonCompleteListener(null);

                String username = socialPerson.name;
                String regId = Needle.userModel.getGcmRegId();
                String id = socialPerson.id;
                String email = socialPerson.email;

                UserVO user = new UserVO(-1, username, email, "", "", regId, socialNetworkId, id);
                user.setCoverPictureURL(socialPerson.coverURL);
                user.setPictureURL(socialPerson.avatarURL);
                Needle.userModel.setUser(user);

                ApiClient.getInstance().login(user.getLoginType(), user.getEmail(), user.getUserName(), user.getGcmRegId(), user.getPassword(), user.getSocialNetworkUserId(), userLogInCallback);
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
                Log.e(TAG, "getSocialProfileAndLogIn( " + networkId + " ) :: Error : " + errorMessage);

                if (tryCount < 3) {
                    Needle.navigationController.updateSplashLabel("It's taking longer than usual ...");
                    tryCount++;
                    if (errorMessage.contains("Unable to resolve host") || errorMessage.contains("statusCode=NETWORK_ERROR")) {
                        socialNetwork.requestCurrentPerson();
                    }
                } else {
                    Needle.navigationController.updateSplashLabel("Something went wrong");
                }

            }
        });

        if(networkId == LOGIN_TYPE_GOOGLE){
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS)
                    == PackageManager.PERMISSION_GRANTED){
                socialNetwork.requestCurrentPerson();
            }else{
                PermissionManager.getInstance(activity).checkAccountsPermission(activity);
                Log.d(TAG, "Accounts permission not granted yet");
            }
        }else{
            socialNetwork.requestCurrentPerson();
        }
    }

    public void getSocialProfileAndRegister(final int networkId) {
        Log.d(TAG, "getSocialProfileAndRegister");
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
        socialNetwork.setOnRequestCurrentPersonCompleteListener(new OnRequestSocialPersonCompleteListener() {
            @Override
            public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
                socialNetwork.setOnRequestSocialPersonCompleteListener(null);

                UserVO userVO = new UserVO();

                switch (socialNetworkId) {
                    case LOGIN_TYPE_FACEBOOK:
                        Log.i(TAG, "Succesfuly retreived social profile of type FACEBOOK");
                        try {
                            socialPerson.coverURL = new GetFacebookCoverURLTask((FacebookSocialNetwork) socialNetwork, socialPerson.id).execute().get();
                            //socialPerson.coverURL = "https://graph.facebook.com/" + socialPerson.id +"?fields=cover&access_token=" + socialNetwork.getAccessToken().token;
                            socialPerson.email = "facebook";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case LOGIN_TYPE_TWITTER:
                        Log.i(TAG, "Succesfuly retreived social profile of type TWITTER");
                        break;
                    case LOGIN_TYPE_GOOGLE:
                           Log.i(TAG, "Succesfuly retreived social profile of type GOOGLE+");
                        break;
                }

                String username = socialPerson.name;
                String regId = Needle.userModel.getGcmRegId();
                String id = socialPerson.id;

                userVO.setUserId(-1);
                userVO.setEmail(socialPerson.email);
                userVO.setUserName(username);
                userVO.setGcmRegId(regId);
                userVO.setLoginType(socialNetworkId);
                userVO.setSocialNetworkUserId(id);
                userVO.setPictureURL(socialPerson.avatarURL);
                userVO.setCoverPictureURL(socialPerson.coverURL);

                Needle.userModel.setUser(userVO);

                ApiClient.getInstance().registerUser(userVO, userRegisteredCallback);
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
                Log.e(TAG, "getSocialProfileAndRegister(" + networkId + ") :: Error : " + errorMessage);
            }
        });
        socialNetwork.requestCurrentPerson();
    }

    public void logOut() {
        Log.i(TAG, "Logging out");
        if(socialNetwork != null){
            socialNetwork.logout();
        }

        Needle.googleApiController.disconnect();
    }

    public void registerWithNetwork(final int networkId) {
        Log.i(TAG, "Registering with network " + networkId);
        socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);

        if(!socialNetwork.isConnected()) {
            if(networkId != 0) {
                Log.i(TAG, "Requesting login on network with id : !" + networkId);
                loginRequestType = LOGIN_REQUEST_TYPE_REGISTER;

                if(socialNetwork.getID() == LOGIN_TYPE_GOOGLE){
                    ((GooglePlusSocialNetwork) socialNetwork).setGoogleApiClient(Needle.googleApiController.getGoogleApiClient());
                    ((GooglePlusSocialNetwork) socialNetwork).setConnectionResult(Needle.googleApiController.getConnectionResult());

                    if(Needle.googleApiController.isConnected()){
                        getSocialProfileAndRegister(LOGIN_TYPE_GOOGLE);
                    }else{
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
                        localBroadcastManager.registerReceiver(googleApiConnectedReceiver, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
                      /*  LocalBroadcastManager.getInstance(activity).unregisterReceiver(googleApiConnectedReceiver);

                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
                        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                ((GooglePlusSocialNetwork) socialNetwork).setGoogleApiClient(Needle.googleApiController.getGoogleApiClient());
                                //socialNetwork.requestLogin(AuthenticationController.this);
                                getSocialProfileAndRegister(networkId);
                            }
                        }, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
*/
                        Needle.googleApiController.init(activity, true);
                    }
                }else{
                    socialNetwork.requestLogin(this);
                }

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

    public void register(UserVO userVO) {
        Needle.userModel.setUser(userVO);
        ApiClient.getInstance().registerUser(userVO, userRegisteredCallback);
    }

    private Callback<UserRegistrationResult> userRegisteredCallback = new Callback<UserRegistrationResult>() {
        @Override
        public void onResponse(Response<UserRegistrationResult> response, Retrofit retrofit) {
            Needle.navigationController.hideProgress();

            UserRegistrationResult result = response.body();
            Needle.userModel.setUserId(result.getUserId());

            UserVO user = Needle.userModel.getUser();
            if(result.getSuccessCode() == 1){
                ApiClient.getInstance().login(user.getLoginType(), user.getEmail(), user.getUserName(), user.getGcmRegId(), user.getPassword(), user.getSocialNetworkUserId(), userLogInCallback);
            }else if(result.getSuccessCode() == 409){
                Toast.makeText(activity, result.getMessage(), Toast.LENGTH_SHORT).show();
            }else{
                Log.d(TAG, "Error with registration!");
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.d(TAG, "User Registration failed : " + t.getMessage());
        }
    };

    private Callback<LoginTaskResult> userLogInCallback = new Callback<LoginTaskResult>() {
        @Override
        public void onResponse(Response<LoginTaskResult> response, Retrofit retrofit) {
            LoginTaskResult result = response.body();

            if(!Needle.userModel.isLoggedIn()) {
                if (result.getSuccessCode() == 1) {
                    Needle.userModel.getUser().setUserId(result.getUser().getUserId());
                    Needle.userModel.saveUser();

                    Needle.userModel.setLoggedIn(true);

                    Needle.navigationController.onPostLogin();

                    Needle.navigationController.setHaystacksCount(result.getHaystackCount());
                    Needle.navigationController.setLocationSharingCount(result.getLocationSharingCount());
                }
                else if(result.getSuccessCode() == 404){
                    Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                    //getSocialProfileAndRegister(result.type);
                }
                else
                {
                    Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Log.i(TAG, "here");

                if(result.getSuccessCode() == 404){
                    Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                    getSocialProfileAndRegister(result.getType());
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.i(TAG, "User login failed : "+t.getMessage());
        }
    };

    public Callback<LoginTaskResult> getUserLoginCallback() {
        return userLogInCallback;
    }

    public boolean hasInitializedSocialNetworks() {
        if(mSocialNetworkManager != null){
            return mSocialNetworkManager.getInitializedSocialNetworks().size() > 0;
        }

        return false;
    }
}
