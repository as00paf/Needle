package com.nemator.needle.fragments.authentication;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "RegisterFragment";

    //Children
    private LinearLayout rootView;
    private EditText user, pass, email;
    private Button registerButton;
    private TextView termsConditionsLink;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        return fragment;
    }

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (LinearLayout) inflater.inflate(R.layout.fragment_register, container, false);

        //Username & Password
        user = (EditText) rootView.findViewById(R.id.input_username);
        pass = (EditText) rootView.findViewById(R.id.input_password);
        email = (EditText) rootView.findViewById(R.id.input_email);

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

        //Setup buttons
        registerButton = (Button) rootView.findViewById(R.id.btn_register);
        termsConditionsLink = (TextView) rootView.findViewById(R.id.link_terms_conditions);

        //Register listeners
        registerButton.setOnClickListener(this);
        termsConditionsLink.setOnClickListener(this);

        return rootView;
    }

    private void register(){
        String username = user.getText().toString();
        String password = pass.getText().toString();
        String emailAddress = email.getText().toString();

        Log.i(TAG, "Trying to register with credentials : " + username + ", " + emailAddress + ", " + password);

        if(validateCredentials(username, password, emailAddress)){
            Needle.userModel.getUser()
                    .setLoginType(AuthenticationController.LOGIN_TYPE_DEFAULT)
                    .setUserName(username)
                    .setEmail(emailAddress)
                    .setPassword(password);

            Needle.authenticationController.register();
        }

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{

        }
    }

    private boolean validateCredentials(String username, String password, String emailAddress) {
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.link_terms_conditions:
                break;
        }
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
