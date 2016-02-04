package com.nemator.needle.fragments.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.utils.AppConstants;

public class LoginSplashFragment extends Fragment {

    private View rootView;
    private TextView progressLabel;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_login_splash, container, false);

        progressLabel = (TextView) rootView.findViewById(R.id.login_splash_label);

        ActionBar actionBar = ((HomeActivity) getActivity()).getSupportActionBar();
        actionBar.hide();

        return rootView;
    }

    public void updateProgressLabel(String progress){
        progressLabel.setText(progress);
    }

}
