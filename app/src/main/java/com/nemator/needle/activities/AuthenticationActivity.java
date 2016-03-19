package com.nemator.needle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.AuthenticationPagerAdapter;
import com.nemator.needle.utils.AppConstants;

public class AuthenticationActivity extends AppCompatActivity {

    public static String TAG = "AuthActivity";

    //View
    private ViewPager viewPager;
    private AuthenticationPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Needle.networkController.init(AuthenticationActivity.this);
        Needle.googleApiController.init(this);
        Needle.userModel.init(this);
        Needle.authenticationController.init(this);
        Needle.gcmController.init(this);

        adapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        initUser(savedInstanceState);
    }

    public void initUser(Bundle  savedInstanceState) {
        if(savedInstanceState != null){
            //TODO : Use constants
            Needle.userModel.setAutoLogin(savedInstanceState.getBoolean("autoLogin", true));
            Needle.userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            Needle.navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState()));
            Needle.navigationController.setPreviousState(savedInstanceState.getInt(AppConstants.APP_PREVIOUS_STATE, Needle.navigationController.getCurrentState()));
        }

        if(Needle.userModel.isLoggedIn()){
            //TODO : manage ?
            Needle.googleApiController.stopAutoManage();
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
        Needle.serviceController.unbindService();
    }
}