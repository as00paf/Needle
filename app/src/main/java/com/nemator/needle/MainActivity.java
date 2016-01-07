package com.nemator.needle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    private Handler handler = new Handler();

    //Service
    private ServiceConnection mConnection;
    private NeedleLocationService locationService;

    //View
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private View content;
    private boolean isServiceConnected = false;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        PermissionManager.getInstance(this).checkLocationPermission(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        setupDrawerLayout();

        handler.post(new Runnable() {
            @Override
            public void run() {
                initService();

                Needle.networkController.init(MainActivity.this);
                if(Needle.networkController.isNetworkConnected()){
                    initUser(savedInstanceState);
                    initView();
                }else{
                    initView();
                    Needle.navigationController.updateSplashLabel(getResources().getString(R.string.no_internet_connection));
                    Needle.navigationController.stopSplashProgress();
                }
            }
        });
    }

    public void initUser(Bundle  savedInstanceState) {
        Needle.authenticationController.init(this);
        Needle.gcmController.init(this);

        //Saved Instance State
        if(savedInstanceState != null){
            Needle.userModel.setAutoLogin(savedInstanceState.getBoolean("autoLogin", true));
            Needle.userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            Needle.navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, Needle.navigationController.getCurrentState()));
        }
    }

    private void initView() {
        if(!Needle.userModel.isLoggedIn()){
            Needle.navigationController.showSection(AppConstants.SECTION_SPLASH_LOGIN);
        }else{
            Needle.userModel.setUser(UserVO.retrieve(getSharedPreferences("Needle", Context.MODE_PRIVATE)));
            Needle.navigationController.onPostLogin();
        }
    }

    protected void initService() {
        //Needle Location Service
        mConnection = new ServiceConnection(){
            public void onServiceConnected(ComponentName className, IBinder service) {
                locationService = ((NeedleLocationService.LocalBinder)service).getService();
                locationService.userModel = Needle.userModel;
                isServiceConnected = true;
            }

            public void onServiceDisconnected(ComponentName className) {
                locationService = null;
                isServiceConnected = false;
            }
        };

        Intent serviceIntent = new Intent(this, NeedleLocationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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
                        String timeLimit = extras.getString(AppConstants.TAG_TIME_LIMIT);
                        String senderName = extras.getString(AppConstants.TAG_SENDER_NAME);
                        int senderId = Integer.parseInt(extras.getString(AppConstants.TAG_SENDER_ID, "-1"));
                        Boolean shareBack = extras.getBoolean(AppConstants.TAG_SHARE_BACK);

                        LocationSharingVO vo = new LocationSharingVO(id, senderName, senderId, timeLimit, shareBack);
                        Needle.navigationController.showReceivedLocationSharing(vo);
                    }else if(type.equals("Haystack")){
                        int owner = Integer.parseInt(extras.getString(AppConstants.TAG_IS_OWNER, "-1"));
                        String name = extras.getString(AppConstants.TAG_HAYSTACK_NAME);
                        String timeLimit = extras.getString(AppConstants.TAG_TIME_LIMIT);
                        int zoneRadius = Integer.parseInt(extras.getString(AppConstants.TAG_ZONE_RADIUS, "-1"));
                        Boolean isCircle = extras.getBoolean(AppConstants.TAG_IS_CIRCLE);
                        Boolean isPublic = extras.getBoolean(AppConstants.TAG_IS_PUBLIC);
                        double latitude = extras.getDouble(AppConstants.TAG_LATITUDE);
                        double longitude = extras.getDouble(AppConstants.TAG_LONGITUDE);
                        LatLng position = new LatLng(latitude, longitude);
                        String pictureURL = extras.getString(AppConstants.TAG_PICTURE_URL);


                        HaystackVO vo = new HaystackVO(id, owner, name, isPublic, timeLimit, zoneRadius, isCircle, position, pictureURL, null, null);
                        Needle.navigationController.showReceivedHaystack(vo);
                    }
                }
            }

            Log.i(TAG, "here");
        }

        super.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if( Needle.navigationController.getHaystackFragment() != null){
            savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY,  Needle.navigationController.getHaystackFragment().getHaystack());
            savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER,  Needle.navigationController.getHaystackFragment().isOwner(Needle.userModel.getUserId()));
        }

        savedInstanceState.putInt(AppConstants.APP_STATE,  Needle.navigationController.getCurrentState());
        savedInstanceState.putBoolean("autoLogin",  Needle.userModel.isAutoLogin());
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
        if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN && state != AppState.REGISTER ){
            Needle.navigationController.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Needle.authenticationController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission Granted !");

        if(requestCode == PermissionManager.PERMISSIONS_REQUEST_ACCESS_ACCOUNTS){
            Needle.authenticationController.logInWithSocialNetwork(Needle.authenticationController.LOGIN_TYPE_GOOGLE);
        }else if(requestCode == PermissionManager.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            Log.d(TAG, "Permission Granted !");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();

        if(isServiceConnected){
            try{
                unbindService(mConnection);
            }catch (Error e){

            }
            isServiceConnected = false;
        }
    }

    //Getters/Setters
    public NeedleLocationService getLocationService() {
        return locationService;
    }

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