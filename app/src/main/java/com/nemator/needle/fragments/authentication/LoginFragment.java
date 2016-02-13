package com.nemator.needle.fragments.authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Children
    private LinearLayout layout;
    private AppCompatEditText usernameText, passwordText;
    private Button mSubmit, twitterButtton;
    private FloatingActionButton facebookButton, googleButton;
    private TextView mRegister;
    private ProgressDialog mProgressDialog;

    //Objects
    private SharedPreferences mSharedPreferences;
    private LoginFragmentInteractionListener fragmentListener;

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
        Needle.authenticationController.initFacebook();
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_login, container, false);

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
        facebookButton = (FloatingActionButton) layout.findViewById(R.id.fab_fb);

        //Twitter
        twitterButtton = (Button) layout.findViewById(R.id.btn_twitter);

        //Google
        googleButton = (FloatingActionButton) layout.findViewById(R.id.fab_google);

        //register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButtton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        Needle.authenticationController.googleSilentSignIn();
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
            case R.id.fab_google:
                networkId = AuthenticationController.LOGIN_TYPE_GOOGLE;
                AuthenticationController.getInstance().googleSignIn();
                break;
            case R.id.fab_fb:
                networkId = AuthenticationController.LOGIN_TYPE_FACEBOOK;
                AuthenticationController.getInstance().facebookLogin(this);
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
            Needle.authenticationController.handleGoogleSignInResult(result);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }

        //Facebook
        Needle.authenticationController.getFacebookCallbackManager().onActivityResult(requestCode, resultCode, data);
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
