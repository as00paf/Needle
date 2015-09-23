package com.nemator.needle.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.MainActivity;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.data.LocationServiceDBHelper.PostLocationRequest;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.haystack.HaystackTask.CreateHaystackResponseHandler;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.tasks.locationSharing.LocationSharingResult;
import com.nemator.needle.tasks.logOut.LogOutTask;
import com.nemator.needle.tasks.logOut.LogOutTask.LogOutResponseHandler;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.RoundedTransformation;
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.authentication.LoginSplashFragment;
import com.nemator.needle.view.authentication.RegisterFragment;
import com.nemator.needle.view.haystack.HaystackFragment;
import com.nemator.needle.view.haystacks.HaystackListFragment;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.locationSharing.LocationSharingListFragment;
import com.nemator.needle.view.locationSharing.createLocationSharing.CreateLocationSharingFragment;
import com.nemator.needle.view.locationSharing.locationSharing.LocationSharingFragment;
import com.nemator.needle.view.settings.SettingsFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static com.nemator.needle.tasks.locationSharing.LocationSharingTask.CreateLocationSharingResponseHandler;
import static com.nemator.needle.view.authentication.LoginFragment.LoginFragmentInteractionListener;
import static com.nemator.needle.view.haystacks.HaystackListFragment.HaystackListFragmentInteractionListener;
import static com.nemator.needle.view.locationSharing.LocationSharingListFragment.LocationSharingListFragmentInteractionListener;

public class NavigationController implements MainActivity.NavigationHandler, OnActivityStateChangeListener,
        HaystackListFragmentInteractionListener, LocationSharingListFragmentInteractionListener,
        CreateHaystackResponseHandler, LoginFragmentInteractionListener, LogOutResponseHandler, CreateLocationSharingResponseHandler{

    private static final String TAG = "NavigationController";

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";

    private static NavigationController instance;

    private MainActivity activity;
    private UserModel userModel;

    //Fragments
    private LoginFragment loginFragment;
    private LoginSplashFragment splashLoginFragment;
    private RegisterFragment registerFragment;
    private HaystackListFragment haystacksListFragment;
    private LocationSharingListFragment locationSharingListFragment;
    private CreateHaystackFragment createHaystackFragment;
    private SettingsFragment settingsFragment;
    private HaystackFragment haystackFragment;
    private CreateLocationSharingFragment createLocationSharingFragment;
    private LocationSharingFragment locationSharingFragment;

    private Menu menu;
    private ActionBar actionBar;
    private View content;

    private int currentState = AppState.LOGIN;
    private int previousState;

    private static ProgressDialog pd;
    private static Context context;
    private android.support.v4.widget.DrawerLayout drawerLayout;
    private FragmentManager manager;
    private NavigationView navigationView;

    private NavigationView.OnNavigationItemSelectedListener navigationItemListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch(menuItem.getItemId()){
                case R.id.drawer_haystacks:
                    showSection(AppConstants.SECTION_HAYSTACKS);
                    break;
                case R.id.drawer_location_sharing:
                    showSection(AppConstants.SECTION_LOCATION_SHARING);
                    break;
                case R.id.drawer_notifications:
                    showSection(AppConstants.SECTION_NOTIFICATIONS);
                    break;
                case R.id.drawer_log_out:
                    logOut();
                case R.id.drawer_settings:
                    showSection(AppConstants.SECTION_SETTINGS);
                case R.id.drawer_help:
                    showSection(AppConstants.SECTION_HELP);
                    break;
            }

            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return false;
        }
    };

    public NavigationController(){
    }

    public static NavigationController getInstance(){
        if(instance == null){
            instance = new NavigationController();
        }

        return instance;
    }

    public void init(MainActivity activity, DrawerLayout drawerLayout, View content){
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.content = content;
        this.userModel = Needle.userModel;

        actionBar = activity.getSupportActionBar();
        manager = activity.getSupportFragmentManager();
    }

    public void showSection(int type){
        int containerViewId = (getCurrentFragment() != null) ? getCurrentFragment().getId() : content.getId();
        Boolean add = false;
        Boolean showActionBar = false;
        Fragment newFragment = null;

        switch(type) {
            case AppConstants.SECTION_SPLASH_LOGIN:
                splashLoginFragment = new LoginSplashFragment();
                add = true;
                newFragment = splashLoginFragment;
                onStateChange(AppState.SPLASH_LOGIN);
                break;
            case AppConstants.SECTION_LOGIN:
                if(loginFragment == null){loginFragment = new LoginFragment();}
                showActionBar = true;
                newFragment = loginFragment;
                onStateChange(AppState.LOGIN);
                break;
            case AppConstants.SECTION_REGISTER:
                if(registerFragment == null){registerFragment = new RegisterFragment();}
                showActionBar = true;
                newFragment = registerFragment;
                onStateChange(AppState.REGISTER);
                break;
            case AppConstants.SECTION_HAYSTACKS:
                showActionBar = true;
                if(haystacksListFragment == null){haystacksListFragment = new HaystackListFragment();}
                newFragment = haystacksListFragment;
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppConstants.SECTION_HAYSTACK:
                if(haystackFragment == null) haystackFragment = new HaystackFragment();
                newFragment = haystackFragment;
                onStateChange(AppState.HAYSTACK);
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                if(createHaystackFragment == null) createHaystackFragment = new CreateHaystackFragment();

                newFragment = createHaystackFragment;
                onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                break;
            case AppConstants.SECTION_LOCATION_SHARING:
                if(locationSharingListFragment == null) locationSharingListFragment = new LocationSharingListFragment();
                newFragment = locationSharingListFragment;
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppConstants.SECTION_CREATE_LOCATION_SHARING:
                if(createLocationSharingFragment == null) createLocationSharingFragment = new CreateLocationSharingFragment();
                newFragment = createLocationSharingFragment;
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppConstants.SECTION_SETTINGS:
                if(settingsFragment == null) settingsFragment = new SettingsFragment();
                newFragment = settingsFragment;
                onStateChange(AppState.SETTINGS);
                break;
        }

        if(showActionBar && !actionBar.isShowing()){
            actionBar.show();
            actionBar.setElevation(0);
        }

        if (newFragment != null){
            if(add){
                manager.beginTransaction()
                        .add(containerViewId, newFragment)
                        .commit();
            }else {
                manager.beginTransaction()
                        .replace(containerViewId, newFragment)
                        .commit();
            }
        }

        System.gc();
    }

    private void removeFragment(Fragment fragment){
        if(fragment == null) return;

        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(fragment);
        trans.commit();
    }

    public void removeLoginSplash(){
        removeFragment(splashLoginFragment);
    }

    @Override
    public void onBackPressed() {
        switch (currentState){
            case AppState.LOGIN:
                activity.onBackPressed();
                break;
            case AppState.REGISTER:
                if(getPreviousState() == AppState.LOGIN){
                    showSection(AppConstants.SECTION_LOGIN);
                    onStateChange(AppState.LOGIN);
                }else{
                    activity.onBackPressed();
                }
                break;
            case AppState.PUBLIC_HAYSTACK_TAB:
                logOut();
                break;
            case AppState.PRIVATE_HAYSTACK_TAB:
                //Goto Public tab
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.CREATE_HAYSTACK_GENERAL_INFOS:
                showSection(AppConstants.SECTION_HAYSTACKS);
                //removeSection(AppConstants.SECTION_CREATE_HAYSTACK);
                break;
            case AppState.CREATE_HAYSTACK_MAP:
                //Goto General Infos
                createHaystackFragment.goToPage(0);
                onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                break;
            case AppState.CREATE_HAYSTACK_USERS:
                //Goto Map
                createHaystackFragment.goToPage(1);
                onStateChange(AppState.CREATE_HAYSTACK_MAP);
                break;
            case AppState.CREATE_HAYSTACK_MAP_SEARCH_OPEN:
                //Close Search
                createHaystackFragment.mCreateHaystackMapFragment.closeSearchResults();
                onStateChange(AppState.CREATE_HAYSTACK_MAP);
                break;
            case AppState.CREATE_HAYSTACK_USERS_SEARCH_OPEN:
                //Close Search
                createHaystackFragment.mCreateHaystackUsersFragment.closeSearchResults();
                onStateChange(AppState.CREATE_HAYSTACK_USERS);
                break;
            case AppState.LOCATION_SHARING_RECEIVED_TAB:
                showSection(AppConstants.SECTION_HAYSTACKS);
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.LOCATION_SHARING_SENT_TAB:
                //Goto Received Tab
                locationSharingListFragment.goToPage(0);
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppState.CREATE_LOCATION_SHARING:
                showSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
                locationSharingListFragment.goToPage(0);
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppState.HAYSTACK:
                //removeSection(AppConstants.SECTION_HAYSTACK);
                showSection(AppConstants.SECTION_HAYSTACKS);
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.LOCATION_SHARING:
                showSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
                //removeSection(AppConstants.SECTION_LOCATION_SHARING);
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppState.SETTINGS:
                removeFragment(settingsFragment);
               // settingsSection.unSelect();
                restorePreviousState();
                break;
            default:
                activity.onBackPressed();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        //if (!activity.isDrawerOpen()) {
            //TODO: use switch statement
            if(getCurrentState() == AppState.HAYSTACK){
                activity.getMenuInflater().inflate(R.menu.haystack, menu);
            }else if(getCurrentState() == AppState.SETTINGS){
                activity.getMenuInflater().inflate(R.menu.menu_settings, menu);
            }else{
                activity.getMenuInflater().inflate(R.menu.menu_haystacks, menu);
            }

            return true;
        //}

       // return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_option_settings:
                showSection(AppConstants.SECTION_SETTINGS);
                return true;
            case R.id.menu_option_help:

                return true;
        }

        return false;
    }

    @Override
    public Menu getMenu() {
        return menu;
    }


    private void logOut(){
        new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(activity.getString(R.string.log_out))
                .setMessage(activity.getString(R.string.log_out_confirmation_msg))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Needle.authenticationController.logOut();
                        new LogOutTask(activity, NavigationController.this).execute();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Deselect logOut section
                       // logOutSection.unSelect();
                    }
                })
                .show();
    }

    @Override
    public void onStateChange(int state) {
        previousState = currentState;
        currentState = state;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    @Override
    public int getCurrentState() {
        return currentState;
    }

    @Override
    public int getPreviousState() {
        return previousState;
    }

    private void restorePreviousState(){
        int state = previousState;
        onStateChange(previousState);

        switch(state){
            case AppState.LOGIN:
                showSection(AppConstants.SECTION_LOGIN);
                break;
            case AppState.PUBLIC_HAYSTACK_TAB :
            case AppState.PRIVATE_HAYSTACK_TAB :
                showSection(AppConstants.SECTION_HAYSTACKS);
                break;
            case AppState.LOCATION_SHARING_SENT_TAB :
            case AppState.LOCATION_SHARING_RECEIVED_TAB :
                showSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
                break;
        }
    }

    @Override
    public void onClickRegister() {
        removeFragment(loginFragment);
        showSection(AppConstants.SECTION_REGISTER);
    }

    //HaystackListFragmentInteractionListener
    @Override
    public void onRefreshHaystackList() {
        haystacksListFragment.fetchHaystacks(true);
    }

    @Override
    public void onClickHaystackCard(HaystackVO haystack) {
        //Add/Remove Sections
        haystackFragment = new HaystackFragment();
        haystackFragment.setHaystack(haystack);
        //haystackSection = activity.newSection(haystack.getName(), R.drawable.ic_haystack, haystackFragment);
        //activity.addSectionAt(haystackSection, 1);

        //showHaystackFragment(haystack.getName());
        showSection(AppConstants.SECTION_HAYSTACK);
    }

    @Override
    public void onCreateHaystackFabTapped() {
        createHaystackFragment = new CreateHaystackFragment();
        showSection(AppConstants.SECTION_CREATE_HAYSTACK);
    }

    //LocationSharingListFragmentInteractionListener
    @Override
    public void onCreateLocationSharingFabTapped() {
        showSection(AppConstants.SECTION_CREATE_LOCATION_SHARING);
    }

    @Override
    public void onClickLocationSharingCard(LocationSharingVO locationSharing, Boolean isSent) {
        //Add/Remove Sections
        locationSharingFragment = new LocationSharingFragment();
        locationSharingFragment.setLocationSharing(locationSharing);
        locationSharingFragment.setIsSent(isSent);
        String name = isSent ? locationSharing.getReceiverName() : locationSharing.getSenderName();
        //locationSharingSection = activity.newSection(name, R.drawable.ic_action_location_found, locationSharingFragment);
        //activity.addSectionAt(locationSharingSection, 2);

        showSection(AppConstants.SECTION_LOCATION_SHARING);
    }

    //TODO:localize
    @Override
    public void onLocationSharingUpdated(LocationSharingResult result) {
        if(result.successCode == 1){
            if(result.vo.getShareBack()){
                activity.getLocationService().startLocationUpdates();
                activity.getLocationService().addPostLocationRequest(PostLocationRequest.POSTER_TYPE_LOCATION_SHARING_BACK,
                        result.vo.getTimeLimit(), result.vo.getSenderId(),
                        String.valueOf(result.vo.getId()));

                Toast.makeText(activity, "Location shared with " + result.vo.getReceiverName(), Toast.LENGTH_SHORT).show();
            }else{
                activity.getLocationService().removePostLocationRequest(PostLocationRequest.POSTER_TYPE_LOCATION_SHARING_BACK,
                                                                        result.vo.getTimeLimit(), result.vo.getSenderId(),
                                                                        String.valueOf(result.vo.getId()));

                Toast.makeText(activity, "Location shared with " + result.vo.getReceiverName(), Toast.LENGTH_SHORT).show();
            }

        }else{
            String msg = result.vo.getShareBack() ? "Location still shared !" : "Could not share location !";
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshLocationSharingList() {
        locationSharingListFragment.fetchLocationSharing();
    }

    //CreateHaystackResponseHandler
    @Override
    public void onHaystackCreated(HaystackTaskResult result){
        if(result.successCode == 0){
            Toast.makeText(activity, "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }else{
            FragmentManager manager = activity.getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(createHaystackFragment);
            trans.commit();

            Toast.makeText(activity, activity.getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

            //Add/Remove Sections
            haystackFragment = new HaystackFragment();
            haystackFragment.setHaystack(result.haystack);
            //haystackSection = activity.newSection(result.haystack.getName(), R.drawable.ic_haystack, haystackFragment);
            //activity.addSectionAt(haystackSection, 1);

            //showHaystackFragment(result.haystack.getName());
            showSection(AppConstants.SECTION_HAYSTACK);
            onStateChange(AppState.HAYSTACK);
        }
    }

    //CreateLocationSharingResponseHandler
    @Override
    public void onLocationSharingCreated(LocationSharingResult result) {
        if(result.successCode == 1){
            activity.getLocationService().startLocationUpdates();
            activity.getLocationService().addPostLocationRequest(PostLocationRequest.POSTER_TYPE_LOCATION_SHARING,
                    result.vo.getTimeLimit(), result.vo.getSenderId(), String.valueOf(result.vo.getId()));
            Toast.makeText(activity, "Location shared with " + result.vo.getReceiverName(), Toast.LENGTH_SHORT).show();
            showSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
        }else{
            Toast.makeText(activity, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
        }
    }

    public void showReceivedLocationSharing(LocationSharingVO vo){
        showSection(AppConstants.SECTION_LOCATION_SHARING);
        locationSharingFragment.setLocationSharing(vo);
        locationSharingFragment.setIsSent(false);
    }

    public void showReceivedHaystack(HaystackVO vo){
        showSection(AppConstants.SECTION_HAYSTACK);
        haystackFragment.setHaystack(vo);
    }

    public void setAccount(){
        TextView username = (TextView) activity.findViewById(R.id.username);
        username.setText(userModel.getUserName());

        ImageView avatarImageView = (ImageView) activity.findViewById(R.id.avatar);
        String pictureURL = userModel.getUser().getPictureURL();
        Picasso.with(activity.getApplicationContext()).load(pictureURL)
                .transform(new CropCircleTransformation())
                .transform(new RoundedTransformation(100, 1))
                .into(avatarImageView);

        int loginType = userModel.getUser().getLoginType();
        Needle.authenticationController.fetchCover(loginType);
    }

    //Getters/Setters
    public HaystackFragment getHaystackFragment() {
        return haystackFragment;
    }

    public void setHaystacksCount(int count){
       // if(count>0)
          //  haystacksSection.setNotifications(count);
    }

    public void setLocationSharingCount(int count){
        //if(count>0)
          //  locationSharingListSection.setNotifications(count);
    }

    @Override
    public void onLogOutComplete() {
        userModel.setLoggedIn(false);
        userModel.setAutoLogin(false);

        showSection(AppConstants.SECTION_LOGIN);
    }

    public LoginFragment getLoginFragment() {
        return loginFragment;
    }

    public static void showProgress(String message) {
        if(context != null){
            pd = new ProgressDialog(context);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage(message);
            //pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }
    }

    public static void hideProgress() {
        if(pd != null){
            pd.dismiss();
        }
    }

    public void updateSplashLabel(String text){
        TextView textView = (TextView) activity.findViewById(R.id.login_splash_label);
        if(textView != null){
            textView.setText(text);
        }
    }

    public void stopSplashProgress(){
        ProgressBar progress = (ProgressBar) activity.findViewById(R.id.login_splash_progress_bar);
        if(progress != null){
            progress.setProgress(0);
        }
    }

    public Fragment getCurrentFragment() {
        switch (getCurrentState()){
            case AppState.SPLASH_LOGIN:
                return splashLoginFragment;
            case AppState.LOGIN:
                return loginFragment;
            case AppState.REGISTER:
                return registerFragment;
            case AppState.PUBLIC_HAYSTACK_TAB:
            case AppState.PRIVATE_HAYSTACK_TAB:
                return haystacksListFragment;
            case AppState.HAYSTACK:
                return haystackFragment;
        }

        return null;
    }

    public void onPostLogin() {
        setAccount();
        showSection(AppConstants.SECTION_HAYSTACKS);

        navigationView = (NavigationView) activity.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navigationItemListener);
    }

    public NavigationView.OnNavigationItemSelectedListener getDrawerListener() {
        return navigationItemListener;
    }

    public void setActionBarTitle(String title){
        actionBar.setTitle(title);
    }
}
