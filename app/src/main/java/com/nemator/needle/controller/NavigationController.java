package com.nemator.needle.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.CreateHaystack;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.data.LocationServiceDBHelper.PostLocationRequest;
import com.nemator.needle.fragments.authentication.LoginFragment;
import com.nemator.needle.fragments.authentication.LoginSplashFragment;
import com.nemator.needle.fragments.authentication.RegisterFragment;
import com.nemator.needle.fragments.haystack.HaystackFragment;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.fragments.locationSharing.LocationSharingListFragment;
import com.nemator.needle.fragments.locationSharing.createLocationSharing.CreateLocationSharingFragment;
import com.nemator.needle.fragments.locationSharing.locationSharing.LocationSharingFragment;
import com.nemator.needle.fragments.people.PeopleFragment;
import com.nemator.needle.fragments.settings.SettingsFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.tasks.locationSharing.LocationSharingResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.CropCircleTransformation;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nemator.needle.fragments.haystacks.HaystackListFragment.HaystackListFragmentInteractionListener;
import static com.nemator.needle.fragments.locationSharing.LocationSharingListFragment.LocationSharingListFragmentInteractionListener;
import static com.nemator.needle.tasks.locationSharing.LocationSharingTask.CreateLocationSharingResponseHandler;

public class NavigationController implements HomeActivity.NavigationHandler, OnActivityStateChangeListener,
        HaystackListFragmentInteractionListener, LocationSharingListFragmentInteractionListener, CreateLocationSharingResponseHandler, Callback<HaystackTaskResult> {

    private static final String TAG = "NavigationController";

    private static NavigationController instance;

    private HomeActivity activity;

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
    private PeopleFragment peopleFragment;

    private Menu menu;
    private ActionBar actionBar;
    private View content;

    private int currentState = AppState.LOGIN;
    private int previousState = -1;

    private ProgressDialog pd;
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
                    return true;
                case R.id.drawer_settings:
                    showSection(AppConstants.SECTION_SETTINGS);
                case R.id.drawer_help:
                    showSection(AppConstants.SECTION_HELP);
                    break;
                case R.id.drawer_people:
                    showSection(AppConstants.SECTION_PEOPLE);
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

    public void init(HomeActivity activity, DrawerLayout drawerLayout, View content){
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.content = content;

        actionBar = activity.getSupportActionBar();
        manager = activity.getSupportFragmentManager();
    }

    public void showSection(int type){
        int containerViewId = (getCurrentFragment() != null) ? getCurrentFragment().getId() : content.getId();
        Boolean add = false;
        Fragment newFragment = null;
        int enterAnimation = 0;
        int exitAnimation = 0;

        switch(type) {
            case AppConstants.SECTION_SPLASH_LOGIN:
                splashLoginFragment = new LoginSplashFragment();
                add = true;
                newFragment = splashLoginFragment;
                onStateChange(AppState.SPLASH_LOGIN);
                break;
            case AppConstants.SECTION_LOGIN:
                if(loginFragment == null){loginFragment = new LoginFragment();}
                newFragment = loginFragment;
                if(previousState != AppState.SPLASH_LOGIN ){
                    enterAnimation = R.anim.enter_from_left;
                    exitAnimation = R.anim.exit_to_right;
                }
                onStateChange(AppState.LOGIN);
                break;
            case AppConstants.SECTION_LOGIN_PICTURE:
                enterAnimation = R.anim.enter_from_right;
                exitAnimation = R.anim.exit_to_left;
                break;
            case AppConstants.SECTION_REGISTER:
                if(registerFragment == null){registerFragment = new RegisterFragment();}
                enterAnimation = R.anim.enter_from_right;
                exitAnimation = R.anim.exit_to_left;
                newFragment = registerFragment;
                onStateChange(AppState.REGISTER);
                break;
            case AppConstants.SECTION_HAYSTACKS:
                if(haystacksListFragment == null){haystacksListFragment = new HaystackListFragment();}
                newFragment = haystacksListFragment;

                if(getCurrentState() < AppState.PUBLIC_HAYSTACK_TAB){
                    actionBar.show();
                }
                actionBar.setTitle(R.string.title_haystacks);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);

                if(previousState == AppState.SETTINGS){
                    enterAnimation = R.anim.enter_from_left;
                    exitAnimation = R.anim.exit_to_right;
                }
                break;
            case AppConstants.SECTION_HAYSTACK:
                if(haystackFragment == null) haystackFragment = new HaystackFragment();
                newFragment = haystackFragment;

                onStateChange(AppState.HAYSTACK);
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                Intent intent = new Intent(activity, CreateHaystack.class);
                activity.startActivity(intent);
                return;
            case AppConstants.SECTION_LOCATION_SHARING:
                if(locationSharingListFragment == null) locationSharingListFragment = new LocationSharingListFragment();
                newFragment = locationSharingListFragment;
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                actionBar.setTitle(R.string.title_location_sharing);
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
                enterAnimation = R.anim.enter_from_right;
                exitAnimation = R.anim.exit_to_left;
                actionBar.setTitle(R.string.title_settings);
                break;
            case AppConstants.SECTION_PEOPLE:
                if(peopleFragment == null) peopleFragment = new PeopleFragment();
                newFragment = peopleFragment;
                onStateChange(AppState.SETTINGS);
                break;
        }

        if (newFragment != null){
            if(add){
                manager.beginTransaction()
                        .add(containerViewId, newFragment)
                        .commit();
            }else {
                manager.beginTransaction()
                        .setCustomAnimations(enterAnimation, exitAnimation)
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
                restorePreviousState();
                removeFragment(settingsFragment);
                break;
            default:
                activity.onBackPressed();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        //TODO: use switch statement
        int state = getCurrentState();
        if(state == AppState.HAYSTACK){
            activity.getMenuInflater().inflate(R.menu.haystack, menu);
        }else if(state == AppState.SETTINGS){
            activity.getMenuInflater().inflate(R.menu.menu_settings, menu);
        }else if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN && state != AppState.REGISTER){
            activity.getMenuInflater().inflate(R.menu.menu_haystacks, menu);
        }

        return true;
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
                        drawerLayout.closeDrawers();
                        ApiClient.getInstance().logout(new Callback<TaskResult>() {
                            @Override
                            public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
                                TaskResult result = response.body();
                                if(result.getSuccessCode() == 1){
                                    Needle.authenticationController.logOut();
                                }else{
                                    Toast.makeText(activity, "Could not log out", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<TaskResult> call, Throwable t) {
                                Needle.authenticationController.logOut();
                            }
                        });
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
        if(currentState != previousState){
            previousState = currentState;
        }
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
        showSection(previousState);
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
    public void onResponse(Call<HaystackTaskResult> call, Response<HaystackTaskResult> response) {
        HaystackTaskResult result = response.body();
        if(result.getSuccessCode() == 0){
            Toast.makeText(activity, "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }else{
            FragmentManager manager = activity.getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(createHaystackFragment);
            trans.commit();

            Toast.makeText(activity, activity.getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

            //Add/Remove Sections
            haystackFragment = new HaystackFragment();
            haystackFragment.setHaystack(result.getHaystack());
            //haystackSection = activity.newSection(result.haystack.getName(), R.drawable.ic_haystack, haystackFragment);
            //activity.addSectionAt(haystackSection, 1);

            //showHaystackFragment(result.haystack.getName());
            showSection(AppConstants.SECTION_HAYSTACK);
            onStateChange(AppState.HAYSTACK);
        }
    }

    @Override
    public void onFailure(Call<HaystackTaskResult> call, Throwable t) {
        Log.d(TAG, "An error occured while creating Haystack : " + t.getMessage());
        Toast.makeText(activity, "An error occured while creating Haystack ", Toast.LENGTH_SHORT).show();
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
        //ActionBar
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);

        activity.getDrawerToggle().setDrawerIndicatorEnabled(true);
        activity.getDrawerToggle().syncState();

        navigationView = (NavigationView) activity.findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);

        //Username
        TextView username = (TextView) headerView.findViewById(R.id.username);
        String name = Needle.userModel.getUserName().substring(0, 1).toUpperCase() + Needle.userModel.getUserName().substring(1);
        username.setText(name);

        //Profile Image
        ImageView avatarImageView = (ImageView) headerView.findViewById(R.id.avatar);
        String pictureURL = Needle.userModel.getUser().getPictureURL().replace("_normal", "");
        if(!TextUtils.isEmpty(pictureURL)){
            Picasso.with(activity.getApplicationContext()).load(pictureURL)
                    .transform(new CropCircleTransformation(activity, 2, Color.WHITE))
                    .into(avatarImageView);
        }else {
            Log.e(TAG, "Can't fetch avatar picture for user " + Needle.userModel.getUserName());
        }

        //Cover Image
        String coverUrl = Needle.userModel.getUser().getCoverPictureURL();
        if(!TextUtils.isEmpty(coverUrl)){
            final ImageView cover = (ImageView) headerView.findViewById(R.id.cover);

            Picasso.with(activity.getApplicationContext())
                    .load(coverUrl)
                    .fit()
                    .into(cover);
        } else {
            Log.e(TAG, "Can't fetch cover for login type " + Needle.userModel.getUser().getLoginType());
        }
    }

    //Getters/Setters
    public HaystackFragment getHaystackFragment() {
        return haystackFragment;
    }

    public void setHaystacksCount(int count) {
        // if(count>0)
        //  haystacksSection.setNotifications(count);
    }

    public void setLocationSharingCount(int count) {
        //if(count>0)
        //  locationSharingListSection.setNotifications(count);
    }

    public void onLogOutComplete() {
        Log.i(TAG, "Log out complete");

        Needle.userModel.setLoggedIn(false);
        Needle.userModel.setAutoLogin(false);

        activity.getDrawerToggle().setDrawerIndicatorEnabled(false);
        activity.getDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_logo_24dp);
        activity.getDrawerToggle().syncState();

        actionBar.hide();

        showSection(AppConstants.SECTION_LOGIN);
    }

    public LoginFragment getLoginFragment() {
        return loginFragment;
    }

    public void showProgress(String message, Boolean cancelable) {
        if(activity != null){
            pd = new ProgressDialog(activity);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage(message);
            pd.setCancelable(cancelable);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }
    }

    public void hideProgress() {
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
            case AppState.SETTINGS:
                return settingsFragment;
        }

        return null;
    }

    public void onPostLogin() {
        showSection(AppConstants.SECTION_HAYSTACKS);

        navigationView = (NavigationView) activity.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navigationItemListener);

        navigationView.getMenu().findItem(R.id.drawer_log_out).setChecked(false);
        navigationView.getMenu().findItem(R.id.drawer_haystacks).setChecked(true);
        //setAccount();
    }

    public NavigationView.OnNavigationItemSelectedListener getDrawerListener() {
        return navigationItemListener;
    }

    public void setActionBarTitle(String title){
        actionBar.setTitle(title);
    }

}
