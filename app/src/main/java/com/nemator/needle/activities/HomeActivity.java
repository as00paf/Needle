package com.nemator.needle.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.LocationVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.PermissionManager;

public class HomeActivity extends AppCompatActivity {

    public static String TAG = "HomeActivity";

    //View
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private View content;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Needle.networkController.init(HomeActivity.this);
        if(Needle.networkController.isNetworkConnected()) {
            initUser(savedInstanceState);
        }

        Needle.googleApiController.init(this);
        initToolbar();
        setupDrawerLayout();

        initView();
        requestPermission();
    }

    private void requestPermission() {
        if(!PermissionManager.getInstance(this).isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            PermissionManager.getInstance(this).requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public void initUser(Bundle  savedInstanceState) {
        Log.d(TAG, "initUser");
        Needle.authenticationController.init(this);
        Needle.gcmController.init(this);

        //Saved Instance State
        if(savedInstanceState != null){
            //TODO : Use constants
            Needle.userModel.setAutoLogin(savedInstanceState.getBoolean("autoLogin", true));
            Needle.userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            Needle.navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState()));
            Needle.navigationController.setPreviousState(savedInstanceState.getInt(AppConstants.APP_PREVIOUS_STATE, Needle.navigationController.getCurrentState()));
        }
    }

    private void initView() {
        Log.d(TAG, "initView");

        UserVO user = UserVO.retrieve(getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE));

        if(!Needle.userModel.isLoggedIn()){
            Needle.navigationController.showSection(AppConstants.SECTION_LOGIN);
        }else {
            Needle.userModel.setUser(user);
            Needle.navigationController.onPostLogin();
        }
    }

    protected void initToolbar() {
        Log.d(TAG, "initToolbar");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        actionBar.hide();
    }

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

        NavigationView appDrawer = (NavigationView) findViewById(R.id.navigation_view);
        appDrawer.setNavigationItemSelectedListener(Needle.navigationController.getDrawerListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_logo_24dp);
        drawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            String action = extras.getString(AppConstants.TAG_ACTION);
            String type = extras.getString(AppConstants.TAG_TYPE);
            int id = Integer.parseInt(extras.getString(AppConstants.TAG_ID, "-1"));

            if(action != null && type != null){
                if(action.equals("Notification")){
                    if(type.equals("LocationSharing")){
                        LocationSharingVO vo = extras.getParcelable(AppConstants.LOCATION_SHARING_DATA_KEY);
                        Needle.navigationController.showReceivedLocationSharing(vo);
                    }else if(type.equals("Haystack")){
                        HaystackVO vo = extras.getParcelable(AppConstants.HAYSTACK_DATA_KEY);

                        Intent haystackIntent = new Intent(this, HaystackActivity.class);
                        haystackIntent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) vo);
                        startActivity(haystackIntent);
                    }
                }
            }

            Log.i(TAG, "here");
        }

        super.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState());
        savedInstanceState.putInt(AppConstants.APP_PREVIOUS_STATE, Needle.navigationController.getPreviousState());
        savedInstanceState.putBoolean("autoLogin", Needle.userModel.isAutoLogin());
        savedInstanceState.putBoolean("loggedIn", Needle.userModel.isLoggedIn());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return Needle.navigationController.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return Needle.navigationController.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int state = Needle.navigationController.getCurrentState();
        if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN ){
            Needle.navigationController.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }

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
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
        Needle.serviceController.unbindService();
    }

    //TODO : move to authentication controller
    public void onClickRevokeGoogleAccess(View view){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.revoke_google_access))
                .setMessage(getString(R.string.revoke_google_access_confirmation_msg))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Needle.authenticationController.revokeGoogleAccess();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }



    //Getters/Setters
    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    public interface NavigationHandler{
        void onBackPressed();
        boolean onCreateOptionsMenu(Menu menu);
        boolean onOptionsItemSelected(MenuItem item);
        Menu getMenu();
    }
}