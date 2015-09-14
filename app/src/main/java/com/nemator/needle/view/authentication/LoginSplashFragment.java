package com.nemator.needle.view.authentication;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;

public class LoginSplashFragment extends Fragment {

    private View rootView;
    private TextView progressLabel;
    private AuthenticationController authenticationController;

    public static LoginSplashFragment newInstance() {
        LoginSplashFragment fragment = new LoginSplashFragment();
        return fragment;
    }

    public LoginSplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticationController = ((MainActivity) getActivity()).getAuthenticationController();
        authenticationController.initSocialNetworkManager(this, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_login_splash, container, false);

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.hide();

        return rootView;
    }

    public void updateProgressLabel(String progress){
        progressLabel.setText(progress);
    }

}
