package com.nemator.needle.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.CropCircleTransformation;
import com.nemator.needle.utils.PermissionManager;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    public static String TAG = "HomeActivity";

    //View
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View content;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Needle.networkController.init(HomeActivity.this);
        initUser(savedInstanceState);

        initToolbar();
        setupDrawerLayout();

        setAccount();
        requestPermission();

        if(!Needle.googleApiController.isConnected()){
            Needle.googleApiController.init(this);
        }

        initNotificationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Needle.serviceController.initService(this);
    }

    private void requestPermission() {
        if(!PermissionManager.getInstance(this).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            PermissionManager.getInstance(this).requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public void initUser(Bundle  savedInstanceState) {
        //Saved Instance State
        if(savedInstanceState != null){
            //TODO : Use constants
            Needle.userModel.init(this);
            Needle.userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            Needle.navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState()));
            Needle.navigationController.setPreviousState(savedInstanceState.getInt(AppConstants.APP_PREVIOUS_STATE, Needle.navigationController.getCurrentState()));
        }
    }

    protected void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    public void setAccount(){
        View headerView = navigationView.getHeaderView(0);

        //Profile Image
        ImageView avatarImageView = (ImageView) headerView.findViewById(R.id.avatar);
        String pictureURL = Needle.userModel.getUser().getPictureURL().replace("_normal", "");
        if(!TextUtils.isEmpty(pictureURL)){
            Picasso.with(getApplicationContext()).load(pictureURL)
                    .transform(new CropCircleTransformation(this, 2, Color.WHITE))
                    .into(avatarImageView);
        }else {
            Log.e(TAG, "Can't fetch avatar picture for user " + Needle.userModel.getUserName());
        }

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
            }
        });

        //Cover Image
        String coverUrl = Needle.userModel.getUser().getCoverPictureURL();
        if(!TextUtils.isEmpty(coverUrl)){
            final ImageView cover = (ImageView) headerView.findViewById(R.id.cover);

            Picasso.with(this)
                    .load(coverUrl)
                    .fit()
                    .into(cover);
        } else {
            Log.e(TAG, "Can't fetch cover for login type " + Needle.userModel.getUser().getLoginType());
        }

        //Username
        TextView username = (TextView) headerView.findViewById(R.id.username);
        username.setText(Needle.userModel.getUserName());

        //Logged in with ...
        TextView accountType = (TextView) headerView.findViewById(R.id.account_type);
        String type = getString(R.string.logged_in_with_needle_account);

        switch (Needle.userModel.getUser().getLoginType()){
            case AuthenticationController.LOGIN_TYPE_GOOGLE:
                type = getString(R.string.logged_in_with_google_account);
                break;
            case AuthenticationController.LOGIN_TYPE_FACEBOOK:
                type = getString(R.string.logged_in_with_facebook_account);
                break;
            case AuthenticationController.LOGIN_TYPE_TWITTER:
                type = getString(R.string.logged_in_with_twitter_account);
                break;
        }

        accountType.setText(type);
    }

    private void initNotificationListener(){
        //TODO : stop listening ?
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(notificationReceiver,
                new IntentFilter(AppConstants.TAG_NOTIFICATION));
    }

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Needle.navigationController.refreshNeedleList();
            Needle.navigationController.refreshHaystackList();
            Needle.navigationController.refreshNotificationList();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerLayout() {
        Log.d(TAG, "setupDrawerLayout");
        content = findViewById(R.id.content);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Needle.navigationController.init(this, drawerLayout, content);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(Needle.navigationController.getNavigationItemListener());

        navigationView.getMenu().findItem(R.id.drawer_needle).setChecked(true);
        navigationView.getMenu().findItem(R.id.drawer_log_out).setChecked(false);
        if(getIntent() != null && getIntent().getExtras() != null){
            LoginResult result = (LoginResult) getIntent().getExtras().get("loginResult");//TODO : use constant
            if(result != null){
                setHaystacksCount(result.getHaystackCount());
                setNeedleCount(result.getLocationSharingCount());
                setNotificationsCount(result.getNotificationCount());
            }
        }else{
            ApiClient.getInstance().getUserInfos(Needle.userModel.getUser(), userInfosCallback);
        }

        Needle.navigationController.showSection(AppConstants.SECTION_NEEDLES);
        showNavigationDrawerIfFirstTime();
    }

    private Callback<LoginResult> userInfosCallback = new Callback<LoginResult>() {
        @Override
        public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
            LoginResult result = response.body();

            if(result.getSuccessCode() > 0){
                Log.d(TAG, "User infos retrieved successfully");
                setHaystacksCount(result.getHaystackCount());
                setNeedleCount(result.getLocationSharingCount());
                setNotificationsCount(result.getNotificationCount());
            }else{
                Log.d(TAG, "Failed to retrieve user infos. Error " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<LoginResult> call, Throwable t) {
            Log.d(TAG, "Failed to retrieve user infos. Error : " + t.getMessage());
        }
    };

    private void showNavigationDrawerIfFirstTime() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                SharedPreferences preferences = getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE);
                boolean isFirstStart = preferences.getBoolean(AppConstants.IS_FIRST_START, true);

                if(isFirstStart) {
                    drawerLayout.openDrawer(findViewById(R.id.navigation_drawer_container));
                    SharedPreferences.Editor e = preferences.edit();
                    e.putBoolean(AppConstants.IS_FIRST_START, false);
                    e.commit();
                }
            }
        });

        t.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            String action = extras.getString(AppConstants.TAG_ACTION);

            if(action.equals(AppConstants.TAG_SECTION)){
                int section =  extras.getInt(AppConstants.TAG_SECTION);
                Needle.navigationController.showSection(section, extras);
            }

            Log.i(TAG, "here");
        }

        super.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState());
        savedInstanceState.putInt(AppConstants.APP_PREVIOUS_STATE, Needle.navigationController.getPreviousState());
        savedInstanceState.putBoolean("loggedIn", Needle.userModel.isLoggedIn());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return Needle.navigationController.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_option_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_option_help:
                startActivity(new Intent(this, HelpSupportActivity.class));
                return true;
        }

        return false;
    }

    /*@Override
    public void onBackPressed() {
        int state = Needle.navigationController.getCurrentState();
        if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN ){
            Needle.navigationController.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        Needle.authenticationController.onGoogleActivityResult(requestCode, resultCode, data);
        Needle.authenticationController.onTwitterActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission Granted !");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Needle.serviceController.unbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
        Needle.googleApiController.disconnect();
    }

    public void setHaystacksCount(int count) {
        setMenuCounter(R.id.drawer_haystacks, count);
    }

    public void setNeedleCount(int count) {
        setMenuCounter(R.id.drawer_needle, count);
    }

    public void setNotificationsCount(int count) {
        setMenuCounter(R.id.drawer_notifications, count);
    }

    public void setFriendRequestsCount(int count) {
        setMenuCounter(R.id.drawer_people, count);
    }

    private void setMenuCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView();
        if(view != null) view.setText(count > 0 ? String.valueOf(count) : null);
    }


    //Getters/Setters
    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    public interface NavigationHandler{
        void onBackPressed();
        boolean onCreateOptionsMenu(Menu menu);
        Menu getMenu();
    }
}