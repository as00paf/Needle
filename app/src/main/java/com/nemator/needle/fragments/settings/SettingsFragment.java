package com.nemator.needle.fragments.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.utils.PermissionManager;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        addPreferencesFromResource(R.xml.preference_screen);
        //toggleNotificationPreferences();
        //setInitialValues();

        return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);

    }

    private void setInitialValues() {
        Boolean isLocationPermissionGranted = PermissionManager.getInstance(getContext()).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);

        SwitchPreference locationPref = (SwitchPreference) findPreference(getString(R.string.pref_key_location_permission));
        locationPref.setChecked(isLocationPermissionGranted);
        locationPref.setSummary(getString(isLocationPermissionGranted ? R.string.permission_granted_msg : R.string.permission_denied_msg, locationPref.getTitle()));
        locationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(((SwitchPreference) preference).isChecked()){//Grant
                    PermissionManager.getInstance(getContext()).requestPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                }else{//Revoke
                    PermissionManager.getInstance(getContext()).startInstalledAppDetailsActivity(getActivity());
                    ((SwitchPreference) preference).setChecked(false);
                }

                return true;
            }
        });

        Boolean isStoragePermissionGranted = PermissionManager.getInstance(getContext()).isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        SwitchPreference storagePref = (SwitchPreference) findPreference(getString(R.string.pref_key_storage_permission));
        storagePref.setChecked(isStoragePermissionGranted);
        storagePref.setSummary(getString(isStoragePermissionGranted ? R.string.permission_granted_msg : R.string.permission_denied_msg, storagePref.getTitle()));
        storagePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(((SwitchPreference) preference).isChecked()){//Grant
                    PermissionManager.getInstance(getContext()).requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }else{//Revoke
                    PermissionManager.getInstance(getContext()).startInstalledAppDetailsActivity(getActivity());
                    ((SwitchPreference) preference).setChecked(false);
                }

                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_show_notifications))) {
            toggleNotificationPreferences();
        }
    }

    private void toggleNotificationPreferences() {
        SwitchPreference preference = (SwitchPreference) findPreference(getString(R.string.pref_key_show_notifications));

        for(String prefKey:getPreferenceManager().getSharedPreferences().getAll().keySet()){
            SwitchPreference pref = (SwitchPreference) findPreference(prefKey);
            if(pref != null && !prefKey.equals(getString(R.string.pref_key_show_notifications)) && prefKey.indexOf("notification") > 0){
                pref.setEnabled(preference.isChecked());
                pref.setChecked(preference.isChecked());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setInitialValues();
    }
}
