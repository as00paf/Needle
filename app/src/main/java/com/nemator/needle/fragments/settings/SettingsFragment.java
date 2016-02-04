package com.nemator.needle.fragments.settings;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.controller.GoogleAPIController;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    private Button googleButton;

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.fragment_settings, container, false);

        //General Preferences
        addHeader(R.string.app_settings);
        addPreferencesFromResource(R.xml.pref_general);

        if(Needle.userModel.getUser().getLoginType() == AuthenticationController.LOGIN_TYPE_GOOGLE){
            //Google Preferences
            addHeader(R.string.google_account);
            addPreferencesFromResource(R.xml.pref_google);

            Preference googlePref = findPreference("google_layout");
        }

        return layout;
    }

    private void addHeader(int titleResId){
        addPreferencesFromResource(R.xml.pref_empty);
        PreferenceCategory header = new PreferenceCategory(getActivity());
        header.setTitle(titleResId);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_empty);
    }


}
