package com.nemator.needle.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.models.vo.FacebookUserVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationController {

    private static final String TAG = "AuthenticationCtrl";
    private static AuthenticationController instance;

    public static final int LOGIN_TYPE_DEFAULT = 0;
    public static final int LOGIN_TYPE_FACEBOOK = 1;
    public static final int LOGIN_TYPE_GOOGLE = 2;
    public static final int LOGIN_TYPE_TWITTER = 3;

    public static final int RC_GOOGLE_SIGN_IN = 9001;
    public static final int RC_GOOGLE_REGISTRATION_SIGN_IN = 9002;

    private AppCompatActivity activity;

    //Facebook
    private final CallbackManager facebookCallbackManager = CallbackManager.Factory.create();
    private ProfileTracker profileTracker;

    //Twitter
    private TwitterAuthClient twitterAuthClient;

    private ProgressDialog mProgressDialog;


    public AuthenticationController(){

    }

    public static AuthenticationController getInstance() {
        if(instance == null){
            instance = new AuthenticationController();
        }

        return instance;
    }

    public void init(AppCompatActivity activity){
        this.activity = activity;
    }

    //Handlers
    private Callback<LoginResult> loginCallback = new Callback<LoginResult>() {
        @Override
        public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
            LoginResult result = response.body();

            Log.i("Needle loginCallback", "Needle loginCallback success : " +(result.getSuccessCode() == 1) );

            if (result.getSuccessCode() == 1) {
                Needle.userModel.setLoggedIn(true);
                Needle.userModel.setUserName(result.getUser().getUserName());
                Needle.userModel.getUser().setEmail(result.getUser().getEmail());
                Needle.userModel.setUserId(result.getUser().getId());
                Needle.userModel.getUser().setSocialNetworkUserId(result.getUser().getSocialNetworkUserId());
                Needle.userModel.getUser().setLoginType(result.getUser().getLoginType());
                Needle.userModel.getUser().setPictureURL(result.getUser().getPictureURL());
                Needle.userModel.getUser().setCoverPictureURL(result.getUser().getCoverPictureURL());
                Needle.userModel.saveUser();

                //TODO : this is a test, might need to fix
                Needle.googleApiController.stopAutoManage();

                Intent intent = new Intent(activity, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                intent.putExtra("loginResult", result);//TODO : use constant
                activity.startActivity(intent);
            }
            else if(result.getSuccessCode() == 404){
                int loginType = Needle.userModel.getUser().getLoginType();
                if(loginType == LOGIN_TYPE_FACEBOOK || loginType == LOGIN_TYPE_GOOGLE || loginType == LOGIN_TYPE_TWITTER){
                    register();
                }else{
                    Toast.makeText(activity, activity.getString(R.string.user_not_found_message), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<LoginResult> call, Throwable t) {
            Log.d(TAG, "log in failed : " + t.getMessage());
            Toast.makeText(activity, "A server error Occured", Toast.LENGTH_SHORT).show();
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
        }else if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_TWITTER){
            Needle.navigationController.onLogOutComplete();
        }else if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_FACEBOOK){
            if(FacebookSdk.isInitialized()){
                LoginManager.getInstance().logOut();
            }
            Needle.navigationController.onLogOutComplete();
        }
    }

    public void logOut(ResultCallback<Status> callback) {
        Log.i(TAG, "Logging out");

        if(Needle.userModel.getUser().getLoginType() == LOGIN_TYPE_GOOGLE){
           googleSignOut(callback);
        }else if(Needle.userModel.getUser().getLoginType() == LOGIN_TYPE_FACEBOOK){
            Log.i(TAG, "Log out of Facebook");
        }else if(Needle.userModel.getUser().getLoginType() == LOGIN_TYPE_DEFAULT){

        }
    }

    public void register(UserVO userVO) {
        Needle.userModel.setUser(userVO);
        ApiClient.getInstance().registerUser(userVO, userRegistrationCallback);
    }

    public void register() {
        ApiClient.getInstance().registerUser(Needle.userModel.getUser(), userRegistrationCallback);
    }

    //Callbacks
    private Callback<UserRegistrationResult> userRegistrationCallback = new Callback<UserRegistrationResult>(){

        @Override
        public void onResponse(Call<UserRegistrationResult> call, Response<UserRegistrationResult> response) {
            UserRegistrationResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Needle Application Registration Success");
                Needle.userModel.getUser().setId(result.getUserId());
                login();
            }else{
                Log.d(TAG, "Needle Application Registration Failed");
            }
        }

        @Override
        public void onFailure(Call<UserRegistrationResult> call, Throwable t) {
            Log.d(TAG, "Needle Application Registration Failed");
        }
    };

    public void login() {
        Log.d(TAG, "Needle Application Login");
        UserVO user = Needle.userModel.getUser();
        ApiClient.getInstance()
                .login(user, loginCallback);
    }

    public void googleSignIn() {
        Log.d(TAG, "Google Login");
        if(Needle.googleApiController.isConnected()){
            Log.d(TAG, "Google Api is Connected");
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(Needle.googleApiController.getGoogleApiClient());//Todo add method in api controller
            activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        }else{
            Log.d(TAG, "Google Api is not Connected");
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            localBroadcastManager.registerReceiver(apiReconnectReceiver, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));

            //TODO : check if necessary
            Needle.googleApiController.init(activity);
        }
    }

    private final BroadcastReceiver apiReconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Google Sign In");

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            localBroadcastManager.unregisterReceiver(this);

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(Needle.googleApiController.getGoogleApiClient());
            activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        }
    };

    public void onGoogleActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Needle.authenticationController.handleGoogleSignInResult(result);
        }
    }

    public void googleSilentSignIn() {
        //Try to login with Google if did not just log out
        if(Needle.navigationController.getPreviousState() == AppState.LOGIN){
            Log.d(TAG, "GoogleSignInApi.silentSignIn");
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(Needle.googleApiController.getGoogleApiClient());
            if (opr.isDone()) {
                // If the usernameText's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleGoogleSignInResult(result);
            } else {
                // If the usernameText has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the usernameText silently.  Cross-device
                // single sign-on will occur in this branch.
               // showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        //hideProgressDialog();
                        handleGoogleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    public void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult is Success :" + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "Signed in successfully with Google account");
            // Signed in successfully, save to model and log into application.
            //hideProgressDialog();

            GoogleSignInAccount acct = result.getSignInAccount();
            String photoURL = (acct.getPhotoUrl() != null) ? acct.getPhotoUrl().toString() : "";
            Needle.userModel.getUser()
                    .setLoginType(AuthenticationController.LOGIN_TYPE_GOOGLE)
                    .setUserName(acct.getDisplayName())
                    .setPictureURL(photoURL)
                    .setEmail(acct.getEmail())
                    .setSocialNetworkUserId(acct.getId());

            Plus.PeopleApi.load(Needle.googleApiController.getGoogleApiClient(), acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                @Override
                public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                    if(loadPeopleResult.getPersonBuffer() != null){
                        Person person = loadPeopleResult.getPersonBuffer().get(0);

                        String coverURL = (person.getCover() != null) ? person.getCover().getCoverPhoto().getUrl() : "";
                        Needle.userModel.getUser().setCoverPictureURL(coverURL);
                        AuthenticationController.getInstance().login();
                    }else{
                        Log.d(TAG, "wtf : "+loadPeopleResult.getStatus().getStatusMessage());
                    }

                }
            });
        } else {
            // Google sign in unsuccessful
            //TODO : do something here

            Log.d(TAG, "Google sign in unsuccessful : " + result.getStatus().getStatusMessage());
            if(result.getStatus().hasResolution()){
                Log.d(TAG, "Result has resolution, fixing it ...");
                try {
                    result.getStatus().startResolutionForResult(activity, RC_GOOGLE_SIGN_IN);
                } catch (IntentSender.SendIntentException e) {
                    Log.d(TAG, "Result resolution failed : " + e.getMessage());
                    e.printStackTrace();
                }
            }else{
                Log.d(TAG, "Result has no resolution");
            }
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
    private void initFacebook(){
        Log.d(TAG, "initFacebook");
        AppEventsLogger.activateApp(activity);
        FacebookSdk.sdkInitialize(activity.getApplicationContext(), facebookInitializedCallback);
    }

    private final FacebookSdk.InitializeCallback facebookInitializedCallback = new FacebookSdk.InitializeCallback() {
        @Override
        public void onInitialized() {
            Log.d(TAG, "facebook sdk initialized");

            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.setApplicationName(activity.getResources().getString(R.string.app_name));
            FacebookSdk.setApplicationId(activity.getResources().getString(R.string.facebook_app_id));

            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Log.d(TAG, "onCurrentProfileChanged");
                }
            };

            profileTracker.startTracking();

            LoginManager.getInstance().registerCallback(facebookCallbackManager, facebookLoginCallback);

            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends", "email"));
        }
    };

    private final FacebookCallback<com.facebook.login.LoginResult> facebookLoginCallback =  new FacebookCallback<com.facebook.login.LoginResult>() {
        @Override
        public void onSuccess(com.facebook.login.LoginResult loginResult) {
            Log.d(TAG, "Facebook log in success");

            //Todo : own class
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            Log.d(TAG, response.toString());
                           if(response.getError() == null){
                               Log.d(TAG, "Facebook graph request success");

                               Gson gson = new Gson();
                               FacebookUserVO user = gson.fromJson(response.getRawResponse(), FacebookUserVO.class);
                               Needle.userModel.setUserFromFacebookAccount(user);

                               login();
                           }else{
                               Log.d(TAG, "Facebook graph request failure");
                           }

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday, cover,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "Facebook log in canceled");
        }

        @Override
        public void onError(FacebookException exception) {
            Log.d(TAG, "Facebook log in error : " + exception.getMessage());
        }
    };

    public void facebookLogin() {
        initFacebook();
    }


    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    //Twitter
    private TwitterApiClient twitterApiClient;
    private TwitterSession twitterSession;

    public void initTwitterSDK(){
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(AppConstants.TWITTER_API_KEY, AppConstants.TWITTER_API_SECRET);
        Fabric.with(activity, new Twitter(authConfig));
        twitterAuthClient = new TwitterAuthClient();//Needed for activity results, I think ...
    }

    public void twitterSignIn() {
        initTwitterSDK();
        twitterAuthClient.authorize(activity, twitterSessionCallback);
    }

    private com.twitter.sdk.android.core.Callback<TwitterSession> twitterSessionCallback = new com.twitter.sdk.android.core.Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            Twitter.getInstance().getApiClient().getAccountService().verifyCredentials(false, false, new com.twitter.sdk.android.core.Callback<User>() {
                @Override
                public void success(Result<User> result) {
                    Needle.userModel.getUser()
                            .setPictureURL(result.data.profileImageUrl.replace("_normal", ""))
                            .setCoverPictureURL(result.data.profileBannerUrl)
                            .setSocialNetworkUserId(String.valueOf(result.data.id))
                            .setLoginType(LOGIN_TYPE_TWITTER)
                            .setUserName(result.data.screenName);

                    login();
                }

                @Override
                public void failure(TwitterException e) {
                    Log.d(TAG, "Twitter log in error : " + e.getMessage());
                }
            });
        }

        @Override
        public void failure(TwitterException e) {
            Log.d(TAG, "Twitter log in error : " + e.getMessage());
        }
    };

    public void onTwitterActivityResult(int requestCode, int responseCode, Intent intent){
        if(twitterAuthClient != null){
            twitterAuthClient.onActivityResult(requestCode, responseCode, intent);
        }
    }


}
