package com.nemator.needle.view.authentication;

import android.app.Activity;
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

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.tasks.login.LoginTaskParams;
import com.nemator.needle.tasks.register.RegisterTask;
import com.nemator.needle.tasks.register.RegisterTaskParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "RegisterFragment";

    //Children
    private FrameLayout layout;
    private EditText user, pass;
    private Button mRegister;
    private CheckBox rememberMeCheckBox;

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
        user = (EditText) layout.findViewById(R.id.register_user_name_edit_text);
        pass = (EditText) layout.findViewById(R.id.register_password_edit_text);

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

        //Remember me CheckBox
        rememberMeCheckBox = (CheckBox) layout.findViewById(R.id.register_remember_me_checkbox);
        rememberMeCheckBox.setChecked(true);

        //setup buttons
        mRegister = (Button) layout.findViewById(R.id.register_register_button);

        //register listeners
        mRegister.setOnClickListener(this);

        return layout;
    }

    private void register(){
        pass.clearFocus();
        user.clearFocus();

        Log.i(TAG, "Trying to register with credentials : " + user.getText().toString() + ", " + pass.getText().toString());

        String username = user.getText().toString();
        String password = pass.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(getActivity(), "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            RegisterTaskParams params = new RegisterTaskParams(getActivity(), username, password, ((MainActivity) getActivity()).getUserModel().getGcmRegId());
            new RegisterTask(params, (((MainActivity) getActivity()).getAuthenticationController())).execute();
        }
    }

    @Override
    public void onClick(View v) {
        register();
    }
}
