package com.nemator.needle.fragments.authentication;

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

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.AuthenticationActivity;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.utils.AppConstants;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Children
    private LinearLayout layout;
    private AppCompatEditText usernameText, passwordText;
    private Button loginButton;
    private FloatingActionButton facebookButton, googleButton, twitterButtton, registerButton;
    private TextView forgotPasswordLink;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_login, container, false);

        //Username & Password
        usernameText = (AppCompatEditText) layout.findViewById(R.id.input_username);
        SharedPreferences preferences = getContext().getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE);
        String userName = preferences.getString(AppConstants.TAG_EMAIL, "");
        if(!userName.equals("facebook")){
            usernameText.setText(userName);
        }

        passwordText = (AppCompatEditText) layout.findViewById(R.id.input_password);
        passwordText.setText(preferences.getString(AppConstants.TAG_PASSWORD, ""));

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

       //Setup buttons
        loginButton = (Button) layout.findViewById(R.id.btn_login);
        forgotPasswordLink = (TextView) layout.findViewById(R.id.link_forgot_password);
        facebookButton = (FloatingActionButton) layout.findViewById(R.id.fab_fb);
        twitterButtton = (FloatingActionButton) layout.findViewById(R.id.fab_twitter);
        googleButton = (FloatingActionButton) layout.findViewById(R.id.fab_google);
        registerButton = (FloatingActionButton) layout.findViewById(R.id.fab_register);

        //Register listeners
        loginButton.setOnClickListener(this);
        forgotPasswordLink.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButtton.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        return layout;
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
            Needle.userModel.getUser()
                    .setLoginType(AuthenticationController.LOGIN_TYPE_DEFAULT)
                    .setUserName(username)
                    .setEmail(email)
                    .setPassword(password);

            Needle.authenticationController.login();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                return;
            case R.id.link_forgot_password:
                forgotPassword();
                return;
            case R.id.fab_twitter:
                Needle.authenticationController.twitterSignIn();
                break;
            case R.id.fab_google:
                Needle.authenticationController.googleSignIn();
                break;
            case R.id.fab_fb:
                Needle.authenticationController.facebookLogin(this);
                break;
            case R.id.fab_register:
                ((AuthenticationActivity) getActivity()).goToRegister();
                break;
            default:
                break;
        }
    }

    private void forgotPassword() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        //Facebook
        if(Needle.authenticationController.getFacebookCallbackManager() != null){
            Needle.authenticationController.getFacebookCallbackManager().onActivityResult(requestCode, resultCode, data);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
