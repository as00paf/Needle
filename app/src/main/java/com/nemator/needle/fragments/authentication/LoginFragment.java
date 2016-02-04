package com.nemator.needle.fragments.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.controller.GoogleAPIController;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Children
    private FrameLayout layout;
    private AppCompatEditText usernameText, passwordText;
    private Button mSubmit, twitterButtton;
    private LoginButton facebookButton;
    private SignInButton googleButton;
    private TextView mRegister;
    private ProgressDialog mProgressDialog;

    //Objects
    private SharedPreferences mSharedPreferences;
    private LoginFragmentInteractionListener fragmentListener;

    //Facebook
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fragmentListener = ((LoginFragmentInteractionListener) Needle.navigationController);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Facebook
        //TODO : move to Authentication Controller
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged( Profile oldProfile, Profile currentProfile) {
                Log.d(TAG, "onCurrentProfileChanged");

                if(currentProfile != null){
                    Needle.userModel.getUser()
                            .setUserName(currentProfile.getName())
                            .setSocialNetworkUserId(currentProfile.getId())
                            .setLoginType(AuthenticationController.LOGIN_TYPE_FACEBOOK);

                    //TODO : if user not found, create it and do the same for google
                    AuthenticationController.getInstance().login();
                }
            }
        };

        layout = (FrameLayout) inflater.inflate(R.layout.fragment_login, container, false);

        mSharedPreferences = getActivity().getSharedPreferences("Needle", Context.MODE_PRIVATE);

        //Username & Password
        usernameText = (AppCompatEditText) layout.findViewById(R.id.usernameEditText);
        String userName = mSharedPreferences.getString(AppConstants.TAG_EMAIL, "");
        if(!userName.equals("facebook")){
            usernameText.setText(userName);
        }

        passwordText = (AppCompatEditText) layout.findViewById(R.id.input_password);
        passwordText.setText(mSharedPreferences.getString(AppConstants.TAG_PASSWORD, ""));

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    login();
                    handled = true;
                }
                return handled;
            }
        });

       //setup buttons
        mSubmit = (Button) layout.findViewById(R.id.btn_login);
        mRegister = (TextView) layout.findViewById(R.id.link_signup);

        //Facebook
        facebookButton = (LoginButton) layout.findViewById(R.id.btn_facebook);
        facebookButton.setReadPermissions("user_friends");
        facebookButton.setFragment(this);

        // Callback registration
        facebookButton.registerCallback(callbackManager, Needle.authenticationController.getFacebookLoginCallback());

        twitterButtton = (Button) layout.findViewById(R.id.btn_twitter);

        //Google
        googleButton = (SignInButton) layout.findViewById(R.id.btn_google);
        googleButton.setSize(SignInButton.SIZE_STANDARD);
        googleButton.setScopes(GoogleAPIController.getInstance().getGSO().getScopeArray());

        //register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        twitterButtton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Try to login with Google if did not just log out
        if(Needle.navigationController.getPreviousState() == AppState.LOGIN){
            Log.d(TAG, "GoogleSignInApi.silentSignIn");
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(Needle.googleApiController.getGoogleApiClient());
            if (opr.isDone()) {
                // If the usernameText's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the usernameText has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the usernameText silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @Override
    public void onResume(){
        boolean rememberMe = mSharedPreferences.getBoolean("rememberMe", false);
        boolean autoLogin =Needle.userModel.isAutoLogin();
        boolean willLogin = rememberMe && autoLogin && ! Needle.userModel.isLoggedIn();

        if(!usernameText.getText().toString().isEmpty() && !passwordText.getText().toString().isEmpty() && willLogin){
            //TODO : fix
            //login();
        }

        super.onResume();
    }

    private void login(){
        passwordText.clearFocus();
        usernameText.clearFocus();

        String userOrEmail = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        Boolean isEmail = userOrEmail.contains("@");
        String username = isEmail ? null : userOrEmail;
        String email = isEmail ? userOrEmail : null;

        Log.i(TAG, "Trying to login with credentials : " + username + ", " + email + ", " + password);

        if(TextUtils.isEmpty(userOrEmail)|| TextUtils.isEmpty(password)){
            //TODO : Localization
            Toast.makeText(getActivity(), "You must enter a username or email and a password", Toast.LENGTH_LONG).show();
        }else{
            UserVO user = new UserVO(-1, username, email, password, "", Needle.userModel.getGcmRegId(), AuthenticationController.LOGIN_TYPE_DEFAULT, "-1");
            Needle.userModel.setUser(user);

            Needle.authenticationController.login();
            //ApiClient.getInstance().login(0, email, username, regId, password, "");
        }
    }

    @Override
    public void onClick(View v) {
        int networkId = 0;
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                return;
            case R.id.link_signup:
                fragmentListener.onClickRegister();
                return;
            case R.id.btn_twitter:
                networkId = AuthenticationController.LOGIN_TYPE_TWITTER;
                break;
            case R.id.btn_google:
                networkId = AuthenticationController.LOGIN_TYPE_GOOGLE;
                AuthenticationController.getInstance().googleSignIn();
                break;

            default:
                break;
        }

        if(networkId != 0){
            //Social Login
            //AuthenticationController.getInstance().login(networkId);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == AuthenticationController.RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

        //Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "Signed in successfully with Google account");
            // Signed in successfully, save to model and log into application.
            hideProgressDialog();

            GoogleSignInAccount acct = result.getSignInAccount();
            Needle.userModel.getUser()
                    .setLoginType(AuthenticationController.LOGIN_TYPE_GOOGLE)
                    .setUserName(acct.getDisplayName())
                    .setEmail(acct.getEmail())
                    .setSocialNetworkUserId(acct.getId());

            AuthenticationController.getInstance().login();
        } else {
            // Google sign in unsuccessful
            //TODO : do something here

            Log.d(TAG, "Google sign in unsuccessful");
            hideProgressDialog();

        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.signing_in));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public interface LoginFragmentInteractionListener{
        void onClickRegister();
    }
}
