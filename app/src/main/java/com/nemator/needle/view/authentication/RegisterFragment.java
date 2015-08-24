package com.nemator.needle.view.authentication;

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

import com.google.android.gms.common.SignInButton;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.user.UserTask;
import com.nemator.needle.tasks.user.UserTaskParams;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "RegisterFragment";

    //Children
    private FrameLayout layout;
    private EditText user, pass;
    private Button registerButton, facebookButton, twitterButton;
    private SignInButton googleButton;

    private AuthenticationController authenticationController;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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

        //register listeners
        registerButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        twitterButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        authenticationController = ((MainActivity) getActivity()).getAuthenticationController();
        authenticationController.initSocialNetworkManager(this);

        return layout;
    }

    private void register(){
        Log.i(TAG, "Trying to register with credentials : " + user.getText().toString() + ", " + pass.getText().toString());

        String username = user.getText().toString();
        String password = pass.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            UserVO userVO = new UserVO(-1, username, password, null, ((MainActivity) getActivity()).getUserModel().getGcmRegId(), AuthenticationController.LOGIN_TYPE_DEFAULT);
            UserTaskParams params = new UserTaskParams(getActivity(), UserTaskParams.TYPE_REGISTER, userVO);
            new UserTask(params, (((MainActivity) getActivity()).getAuthenticationController())).execute();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.register_btn_facebook:
                authenticationController.registerWithNetwork(AuthenticationController.LOGIN_TYPE_FACEBOOK);
                break;
            case R.id.register_btn_twitter:
                authenticationController.registerWithNetwork(AuthenticationController.LOGIN_TYPE_TWITTER);
                break;
            case R.id.register_btn_google:
                authenticationController.registerWithNetwork(AuthenticationController.LOGIN_TYPE_GOOGLE);
                break;
        }
    }
}
