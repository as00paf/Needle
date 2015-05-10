package com.nemator.needle;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.nemator.needle.authentication.LoginFragment;
import com.nemator.needle.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;

public class MainActivity extends MaterialNavigationDrawer {
    public static String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;
    public boolean autoLogin = true;

    @Override
    public void init(Bundle savedInstanceState) {
        //Auto-Login
        if(savedInstanceState != null){
            autoLogin = savedInstanceState.getBoolean("autoLogin", true);
        }

        if(getIntent().getExtras() != null){
            autoLogin = getIntent().getExtras().getBoolean("autoLogin");
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        String username = mSharedPreferences.getString("username", "");
        MaterialAccount account;
        if(!username.isEmpty()){
            String email = mSharedPreferences.getString("email", "");
            account = new MaterialAccount(getResources(), username, email, R.drawable.me, R.drawable.mat);
            this.setFirstAccountPhoto(getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
        }else{
            account = new MaterialAccount(getResources(), "Please Log In", "", R.drawable.ic_action_person, R.drawable.mat);
        }

        this.addAccount(account);

        // create sections
        this.addSection(newSection(getString(R.string.login), R.drawable.ic_account, new LoginFragment()));
        this.addSection(newSection(getString(R.string.title_settings), R.drawable.ic_action_settings, new SettingsFragment()));
        this.addSection(newSection(getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));

        //Navigation Drawer
        Boolean firstNavDrawerLearned = mSharedPreferences.getBoolean("firstNavDrawerLearned", false);

        if(firstNavDrawerLearned){
            this.disableLearningPattern();
        }else{
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }
    }
}
