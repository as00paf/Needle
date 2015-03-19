package com.nemator.needle.settings;


import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.preference.PreferenceFragment;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.nemator.needle.R;

public class SettingsFragment extends PreferenceFragment {

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

        //Header
        addPreferencesFromResource(R.xml.pref_empty);
        PreferenceCategory appSettingsHeader = new PreferenceCategory(getActivity());
        appSettingsHeader.setTitle(R.string.app_settings);
        getPreferenceScreen().addPreference(appSettingsHeader);

        //Preferences
        addPreferencesFromResource(R.xml.pref_general);

        return layout;
    }


}
