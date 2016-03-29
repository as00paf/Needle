package com.nemator.needle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.AuthenticationPagerAdapter;
import com.nemator.needle.controller.AuthenticationController;
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

        adapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        Needle.networkController.init(AuthenticationActivity.this);
        Needle.googleApiController.init(this);
        Needle.userModel.init(this);
        Needle.authenticationController.init(this);
        Needle.gcmController.init(this);

        initUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Needle.authenticationController.googleSilentSignIn();
    }

    public void initUser() {
        if(Needle.userModel.isLoggedIn()){
            //TODO : manage ?
            Needle.googleApiController.stopAutoManage();
            finish();
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == AuthenticationController.RC_GOOGLE_SIGN_IN) {
            Needle.authenticationController.onGoogleActivityResult(requestCode, resultCode, data);
        }else{
            Needle.authenticationController.onTwitterActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        Needle.networkController.unregister();
        super.onStop();
    }
}