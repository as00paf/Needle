package com.nemator.needle.view.authentication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.nemator.needle.MainActivity;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.tasks.login.LoginTaskParams;
import com.nemator.needle.utils.AppConstants;

import java.lang.ref.WeakReference;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    //Children
    private FrameLayout layout;
    private EditText user, pass;
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

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Username & Password
        user = (EditText) layout.findViewById(R.id.usernameEditText);
        user.setText(mSharedPreferences.getString(AppConstants.TAG_USER_NAME, ""));

        pass = (EditText) layout.findViewById(R.id.input_password);
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

        String username = user.getText().toString();
        String password = pass.getText().toString();
        String regId = Needle.userModel.getGcmRegId();

        //UserVO user = new UserVO(-1, username, password, "", regId, AuthenticationController.LOGIN_TYPE_DEFAULT, "-1");

        Log.i(TAG, "Trying to login with credentials : " + username + ", " + password + ", " + regId);

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            WeakReference<TextView> textView = new WeakReference<TextView>((TextView) ((MainActivity) getActivity()).findViewById(R.id.login_splash_label));
           // LoginTaskParams params = new LoginTaskParams(getActivity(), user, textView);
            //new LoginTask(params, Needle.authenticationController).execute();

            //ApiClient.getInstance().login(0, );
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
            Needle.authenticationController.logInWithSocialNetwork(networkId);
        }
    }

    public interface LoginFragmentInteractionListener{
        void onClickRegister();
    }
}
