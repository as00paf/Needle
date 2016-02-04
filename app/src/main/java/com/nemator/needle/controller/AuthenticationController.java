package com.nemator.needle.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.UserRegistrationResult;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.login.LoginTaskResult;
import com.nemator.needle.utils.AppConstants;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AuthenticationController {

    private static final String TAG = "AuthenticationCtrl";
    private static AuthenticationController instance;

    public static final int LOGIN_TYPE_DEFAULT = 0;
    public static final int LOGIN_TYPE_FACEBOOK = 1;
    public static final int LOGIN_TYPE_GOOGLE = 2;
    public static final int LOGIN_TYPE_TWITTER = 3;

    public static final int RC_GOOGLE_SIGN_IN = 9001;

    private HomeActivity activity;
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

    public void init(HomeActivity activity){
        this.activity = activity;
    }

    //Handlers
    private Callback<LoginTaskResult> loginCallback = new Callback<LoginTaskResult>() {
        @Override
        public void onResponse(Response<LoginTaskResult> response, Retrofit retrofit) {
            LoginTaskResult result = response.body();
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
            }
            else
            {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.d(TAG, "log in failed");
        }
    };

    //Public Mehtods
    public void logOut() {
        Log.i(TAG, "Logging out from specific type");

        if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_GOOGLE){
            Needle.authenticationController.googleSignOut(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    //TODO : do something here
                    if (status.isSuccess()) {
                        Needle.navigationController.onLogOutComplete();
                    } else {
                        Log.d(TAG, "Could not log out.");
                    }
                }
            });
        }else if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_DEFAULT){
            Needle.navigationController.onLogOutComplete();
        }
    }

    public void logOut(ResultCallback<Status> callback) {
        Log.i(TAG, "Logging out");

        if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_GOOGLE){
           googleSignOut(callback);
        }else if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_DEFAULT){

        }
    }

    public void register(UserVO userVO) {
        Needle.userModel.setUser(userVO);
        ApiClient.getInstance().registerUser(userVO, userRegistrationCallback);
    }

    //Callbacks
    private Callback<UserRegistrationResult> userRegistrationCallback = new Callback<UserRegistrationResult>(){

        @Override
        public void onResponse(Response<UserRegistrationResult> response, Retrofit retrofit) {

        }

        @Override
        public void onFailure(Throwable t) {

        }
    };

    public void login() {
        Log.d(TAG, "Needle Application Login");
        UserVO user = Needle.userModel.getUser();
        ApiClient.getInstance()
                .login(user.getLoginType(), user.getEmail() ,user.getUserName(), user.getGcmRegId(),
                        user.getPassword(), user.getSocialNetworkUserId(), loginCallback);
    }

    public void googleSignIn() {
        if(Needle.googleApiController.isConnected()){
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(Needle.googleApiController.getGoogleApiClient());
            activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        }else{
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            localBroadcastManager.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(Needle.googleApiController.getGoogleApiClient());
                    activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
                }
            }, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));

            Needle.googleApiController.init(activity);
        }
    }

    private void googleSignOut(ResultCallback<Status> callback) {
        Auth.GoogleSignInApi.signOut(Needle.googleApiController.getGoogleApiClient()).setResultCallback(callback);
    }

    public void revokeGoogleAccess() {
        Auth.GoogleSignInApi.revokeAccess(Needle.googleApiController.getGoogleApiClient()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Needle.navigationController.onLogOutComplete();
                } else {
                    //TODO : do something here
                    Log.d(TAG, "Could not revoke access.");
                }
            }
        });
    }


    //Facebook
    private FacebookCallback<LoginResult> facebookLoginCallback =  new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "Facebook log in success");


        }

        @Override
        public void onCancel() {
            // App code
            Log.d(TAG, "Facebook log in canceled");
        }

        @Override
        public void onError(FacebookException exception) {
            // App code

            Log.d(TAG, "Facebook log in error : " + exception.getMessage());
        }
    };

    public FacebookCallback<LoginResult> getFacebookLoginCallback() {
        return facebookLoginCallback;
    }
}
