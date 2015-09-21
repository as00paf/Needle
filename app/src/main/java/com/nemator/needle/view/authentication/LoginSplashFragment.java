package com.nemator.needle.view.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.MainActivity;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import twitter4j.User;

public class LoginSplashFragment extends Fragment {

    private View rootView;
    private TextView progressLabel;
    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver socialNetworksInitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            localBroadcastManager.unregisterReceiver(this);

            if(Needle.userModel.isLoggedIn()){
                Needle.authenticationController.logInWithSocialNetwork(Needle.userModel.getUser().getLoginType());
            }else{
                Needle.navigationController.removeLoginSplash();
                Needle.navigationController.showSection(AppConstants.SECTION_LOGIN);
            }
        }
    };

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

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(socialNetworksInitReceiver, new IntentFilter(AppConstants.SOCIAL_NETWORKS_INITIALIZED));
        Needle.authenticationController.initSocialNetworkManager(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_login_splash, container, false);

        progressLabel = (TextView) rootView.findViewById(R.id.login_splash_label);

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.hide();

        return rootView;
    }

    public void updateProgressLabel(String progress){
        progressLabel.setText(progress);
    }

}
