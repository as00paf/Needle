package com.nemator.needle.view.authentication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

import com.google.android.gms.common.SignInButton;
import com.nemator.needle.MainActivity;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.utils.AppConstants;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Children
    private FrameLayout layout;
    private AppCompatEditText user, pass;
    private Button mSubmit, facebookButton, twitterButtton;
    private SignInButton googleButton;
    private TextView mRegister;

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
        layout = (FrameLayout) inflater.inflate(R.layout.fragment_login, container, false);

        mSharedPreferences = getActivity().getSharedPreferences("Needle", Context.MODE_PRIVATE);

        //Username & Password
        user = (AppCompatEditText) layout.findViewById(R.id.usernameEditText);
        user.setText(mSharedPreferences.getString(AppConstants.TAG_EMAIL, ""));

        pass = (AppCompatEditText) layout.findViewById(R.id.input_password);
        pass.setText(mSharedPreferences.getString(AppConstants.TAG_PASSWORD, ""));

        /*if(!((MaterialNavigationDrawer) getActivity()).isDrawerOpen()){
            if(TextUtils.isEmpty(user.getText())){
                user.requestFocus();
            }else{
                pass.requestFocus();
            }
        }*/

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        facebookButton = (Button) layout.findViewById(R.id.btn_facebook);
        twitterButtton = (Button) layout.findViewById(R.id.btn_twitter);
        googleButton = (SignInButton) layout.findViewById(R.id.btn_google);

        //register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButtton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        Needle.authenticationController.initSocialNetworkManager(this);

        return layout;
    }

    @Override
    public void onResume(){
        boolean rememberMe = mSharedPreferences.getBoolean("rememberMe", false);
        boolean autoLogin =Needle.userModel.isAutoLogin();
        boolean willLogin = rememberMe && autoLogin && ! Needle.userModel.isLoggedIn();

        if(!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty() && willLogin){
            login();
        }

        super.onResume();
    }

    private void login(){
        pass.clearFocus();
        user.clearFocus();

        String userOrEmail = user.getText().toString();
        String password = pass.getText().toString();
        String regId = Needle.userModel.getGcmRegId();

        Boolean isEmail = userOrEmail.contains("@");
        String username = isEmail ? null : userOrEmail;
        String email = isEmail ? userOrEmail : null;

        //UserVO user = new UserVO(-1, username, password, "", regId, AuthenticationController.LOGIN_TYPE_DEFAULT, "-1");

        Log.i(TAG, "Trying to login with credentials : " + username + ", " + email + ", " + password + ", " + regId);

        if(TextUtils.isEmpty(userOrEmail)|| TextUtils.isEmpty(password)){
            //TODO : Localization
            Toast.makeText(getActivity(), "You must enter a username or email and a password", Toast.LENGTH_LONG).show();
        }else{
            ApiClient.getInstance().login(0, email, username, regId, password, "", Needle.authenticationController.getUserLoginCallback());
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
            case R.id.btn_facebook:
                networkId = AuthenticationController.LOGIN_TYPE_FACEBOOK;
                break;
            case R.id.btn_twitter:
                networkId = AuthenticationController.LOGIN_TYPE_TWITTER;
                break;
            case R.id.btn_google:
                networkId = AuthenticationController.LOGIN_TYPE_GOOGLE;
                break;

            default:
                break;
        }

        if(networkId != 0){
            if(networkId == AuthenticationController.LOGIN_TYPE_GOOGLE){
                if(!Needle.googleApiController.getBuildPlus()){
                    LocalBroadcastManager.getInstance(getActivity())
                            .registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    Needle.authenticationController.logInWithSocialNetwork(AuthenticationController.LOGIN_TYPE_GOOGLE);
                                }
                            }, new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));

                    Needle.googleApiController.reinitWithPlus(((MainActivity) getActivity()));
                }else{
                    Needle.authenticationController.logInWithSocialNetwork(networkId);
                }
            }else{
                Needle.authenticationController.logInWithSocialNetwork(networkId);
            }
        }
    }

    public interface LoginFragmentInteractionListener{
        void onClickRegister();
    }
}
