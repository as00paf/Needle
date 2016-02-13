package com.nemator.needle.fragments.authentication;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.controller.GoogleAPIController;
import com.nemator.needle.models.vo.UserVO;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "RegisterFragment";

    //Children
    private FrameLayout layout;
    private EditText user, pass, email;
    private Button registerButton, facebookButton, twitterButton;
    private SignInButton googleButton;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.fragment_register, container, false);

        //Username & Password
        user = (EditText) layout.findViewById(R.id.register_input_username);
        pass = (EditText) layout.findViewById(R.id.register_input_password);
        email = (EditText) layout.findViewById(R.id.register_input_email);

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    register();
                    handled = true;
                }
                return handled;
            }
        });

        //setup buttons
        registerButton = (Button) layout.findViewById(R.id.btn_register);
        facebookButton = (Button) layout.findViewById(R.id.register_btn_facebook);
        twitterButton = (Button) layout.findViewById(R.id.register_btn_twitter);

        googleButton = (SignInButton) layout.findViewById(R.id.register_btn_google);
        googleButton.setSize(SignInButton.SIZE_STANDARD);
        googleButton.setScopes(GoogleAPIController.getInstance().getGSO().getScopeArray());

        //register listeners
        registerButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        return layout;
    }

    private void register(){
        String username = user.getText().toString();
        String password = pass.getText().toString();
        String emailAddress = email.getText().toString();

        Log.i(TAG, "Trying to register with credentials : " + username + ", " + emailAddress + ", " + password);

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            UserVO userVO = new UserVO(-1, username, emailAddress, password, "", Needle.userModel.getGcmRegId(), AuthenticationController.LOGIN_TYPE_DEFAULT, "-1");
            userVO.setCoverPictureURL("");
            Needle.authenticationController.register(userVO);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.register_btn_facebook:
                //Needle.authenticationController.registerWithNetwork(AuthenticationController.LOGIN_TYPE_FACEBOOK);
                break;
            case R.id.register_btn_twitter:
                //Needle.authenticationController.registerWithNetwork(AuthenticationController.LOGIN_TYPE_TWITTER);
                break;
            case R.id.register_btn_google:
                AuthenticationController.getInstance().googleSignIn();
                break;
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
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "Signed in successfully with Google account");
            // Signed in successfully, save to model and log into application.
            //hideProgressDialog();

            GoogleSignInAccount acct = result.getSignInAccount();
            Needle.userModel.getUser()
                    .setLoginType(AuthenticationController.LOGIN_TYPE_GOOGLE)
                    .setUserName(acct.getDisplayName())
                    .setEmail(acct.getEmail())
                    .setSocialNetworkUserId(acct.getId());

            AuthenticationController.getInstance().register();
        } else {
            // Google sign in unsuccessful
            //TODO : do something here

            Log.d(TAG, "Google sign in unsuccessful");
            // hideProgressDialog();

        }
    }

}
