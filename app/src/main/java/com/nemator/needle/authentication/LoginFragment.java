package com.nemator.needle.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.AppConstants;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.authentication.task.AuthenticationResult;
import com.nemator.needle.authentication.task.LoginTask;
import com.nemator.needle.authentication.task.LoginTaskParams;
import com.nemator.needle.home.HaystacksActivity;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class LoginFragment extends Fragment implements View.OnClickListener, LoginTask.LoginResponseHandler{

    private FrameLayout layout;
    private SharedPreferences mSharedPreferences;
    private EditText user, pass;
    private Button mSubmit, mRegister;
    private CheckBox rememberMeCheckBox;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.fragment_login, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Username & Password
        user = (EditText) layout.findViewById(R.id.usernameEditText);
        user.setText(mSharedPreferences.getString("username", ""));

        pass = (EditText) layout.findViewById(R.id.password);
        pass.setText(mSharedPreferences.getString("password", ""));

        if(!((MaterialNavigationDrawer) getActivity()).isDrawerOpen()){
            if(TextUtils.isEmpty(user.getText())){
                user.requestFocus();
            }else{
                pass.requestFocus();
            }
        }

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

        //Remember me CheckBox
        Boolean rememberMe = mSharedPreferences.getBoolean("rememberMe", true);
        rememberMeCheckBox = (CheckBox) layout.findViewById(R.id.rememberMeCheckBox);
        rememberMeCheckBox.setChecked(rememberMe);

        //setup buttons
        mSubmit = (Button) layout.findViewById(R.id.login);
        mRegister = (Button) layout.findViewById(R.id.register);

        //register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);

        return layout;
    }

    @Override
    public void onResume(){
        boolean rememberMe = mSharedPreferences.getBoolean("rememberMe", false);
        boolean autoLogin = ((MainActivity) getActivity()).autoLogin;
        boolean willLogin = rememberMe && autoLogin;

        if(!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty() && willLogin){
            login();
        }

        super.onResume();
    }

    private void login(){
        Log.i(AppConstants.TAG_MESSAGE, "Trying to login with credentials : " + user.getText().toString() + ", " + pass.getText().toString());

        String username = user.getText().toString();
        String password = pass.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            LoginTaskParams params = new LoginTaskParams(username, password, getActivity(), rememberMeCheckBox.isChecked(), false);
            new LoginTask(params, this).execute();
        }
    }

    public void onLoginComplete(AuthenticationResult result){
        if(result.successCode == 1){
            Intent i = new Intent(getActivity(), HaystacksActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }else{
            Toast.makeText(getActivity(), "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.register:
                Intent i = new Intent(getActivity(), RegisterActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }

}
