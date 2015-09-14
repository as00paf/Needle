package com.nemator.needle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import com.nemator.needle.controller.GCMController;
import com.nemator.needle.controller.GoogleAPIController;
import com.nemator.needle.controller.NavigationController;
import com.nemator.needle.controller.NetworkController;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;

    //Service
    private ServiceConnection mConnection;
    private NeedleLocationService locationService;

    //App Components
    private UserModel userModel;
    private AuthenticationController authenticationController;
    private NavigationController navigationController;
    private GCMController gcmController;
    private GoogleAPIController googleApiController;
    private NetworkController networkController;

    //View
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private View content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        setupDrawerLayout();
        initDrawerToggle();
        initService();

        content = findViewById(R.id.content);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        networkController = new NetworkController(this);
        if(networkController.isNetworkConnected()){
            initUser(savedInstanceState);
            initView();
        }else{
            initView();
            navigationController.updateSplashLabel(getResources().getString(R.string.no_internet_connection));
            navigationController.stopSplashProgress();
        }
    }

    public void initUser(Bundle  savedInstanceState) {
        userModel = new UserModel(this);
        navigationController = new NavigationController(this, drawerLayout, content, userModel);
        authenticationController = new AuthenticationController(this, userModel, navigationController);
        gcmController = new GCMController(this, userModel);
        googleApiController = new GoogleAPIController(this);

        //Saved Instance State
        if(savedInstanceState != null){
            userModel.setAutoLogin(savedInstanceState.getBoolean("autoLogin", true));
            userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, navigationController.getCurrentState()));
        }


    }

    private void initView() {
        if(!userModel.isLoggedIn()){
            navigationController.showSection(AppConstants.SECTION_SPLASH_LOGIN);
        }else{
            navigationController.setAccount();
            navigationController.showSection(AppConstants.SECTION_HAYSTACKS);
        }
    }

    protected void initService() {
        //Needle Location Service
        mConnection = new ServiceConnection(){
            public void onServiceConnected(ComponentName className, IBinder service) {
                locationService = ((NeedleLocationService.LocalBinder)service).getService();
                locationService.userModel = userModel;
            }

            public void onServiceDisconnected(ComponentName className) {
                locationService = null;
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
            actionBar.setHomeAsUpIndicator(R.drawable.icon_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }
    }

    private void initDrawerToggle(){
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                Snackbar.make(content, menuItem.getTitle() + " pressed", Snackbar.LENGTH_LONG).show();
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });
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
                        navigationController.showReceivedLocationSharing(vo);
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
                        navigationController.showReceivedHaystack(vo);
                    }
                }
            }

            Log.i(TAG, "here");
        }

        super.onNewIntent(intent);
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(navigationController.getHaystackFragment() != null){
            savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, navigationController.getHaystackFragment().getHaystack());
            savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER, navigationController.getHaystackFragment().isOwner(userModel.getUserId()));
        }

        savedInstanceState.putInt(AppConstants.APP_STATE, navigationController.getCurrentState());
        savedInstanceState.putBoolean("autoLogin", userModel.isAutoLogin());
        savedInstanceState.putBoolean("loggedIn", userModel.isLoggedIn());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return navigationController.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationController.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int state = navigationController.getCurrentState();
        if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN && state != AppState.REGISTER ){
            navigationController.onBackPressed();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authenticationController.onActivityResult(requestCode, resultCode, data);
    }

    //Getters/Setters
    public NeedleLocationService getLocationService() {
        return locationService;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public NavigationController getNavigationController() {
        return navigationController;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public interface NavigationHandler{
        void onBackPressed();
        boolean onCreateOptionsMenu(Menu menu);
        boolean onOptionsItemSelected(MenuItem item);
        Menu getMenu();
    }


}