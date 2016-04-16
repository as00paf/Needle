package com.nemator.needle.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.AuthenticationActivity;
import com.nemator.needle.activities.CreateHaystackActivity;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.fragments.locationSharing.LocationSharingListFragment;
import com.nemator.needle.fragments.locationSharing.createLocationSharing.CreateLocationSharingExpirationFragment;
import com.nemator.needle.fragments.notifications.NotificationFragment;
import com.nemator.needle.fragments.people.PeopleFragment;
import com.nemator.needle.fragments.settings.SettingsFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nemator.needle.fragments.haystacks.HaystackListFragment.HaystackListFragmentInteractionListener;

public class NavigationController implements HomeActivity.NavigationHandler, OnActivityStateChangeListener,
        HaystackListFragmentInteractionListener {

    private static final String TAG = "NavigationController";

    private static NavigationController instance;

    private HomeActivity activity;

    //Fragments
    private HaystackListFragment haystacksListFragment;
    private LocationSharingListFragment locationSharingListFragment;
    private SettingsFragment settingsFragment;
    private CreateLocationSharingExpirationFragment createLocationSharingExpirationFragment;
    private PeopleFragment peopleFragment;
    private NotificationFragment notificationFragment;

    private Menu menu;
    private ActionBar actionBar;
    private View content;

    private int currentState = AppState.LOGIN;
    private int previousState = -1;

    private ProgressDialog pd;
    private android.support.v4.widget.DrawerLayout drawerLayout;
    private FragmentManager manager;

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
        showSection(type, null);
    }

    public void showSection(int type, Bundle bundle){
        //int containerViewId = (getCurrentFragment() != null) ? getCurrentFragment().getId() : content.getId();
        int containerViewId = content.getId();
        Boolean add = containerViewId == 0;
        Fragment newFragment = null;
        int enterAnimation = 0;
        int exitAnimation = 0;

        switch(type) {
            case AppConstants.SECTION_HAYSTACKS:
                //if(haystacksListFragment == null){haystacksListFragment = HaystackListFragment.newInstance();}
                haystacksListFragment = HaystackListFragment.newInstance();
                newFragment = haystacksListFragment;

                if(actionBar != null) actionBar.setTitle(R.string.title_haystacks);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);

                if(previousState == AppState.SETTINGS){
                    enterAnimation = R.anim.enter_from_left;
                    exitAnimation = R.anim.exit_to_right;
                }
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                Intent intent = new Intent(activity, CreateHaystackActivity.class);
                activity.startActivity(intent);
                return;
            case AppConstants.SECTION_LOCATION_SHARING:
                if(locationSharingListFragment == null) locationSharingListFragment = new LocationSharingListFragment();
                newFragment = locationSharingListFragment;
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                actionBar.setTitle(R.string.title_location_sharing);
                break;
            case AppConstants.SECTION_CREATE_LOCATION_SHARING:
                if(createLocationSharingExpirationFragment == null) createLocationSharingExpirationFragment = new CreateLocationSharingExpirationFragment();
                newFragment = createLocationSharingExpirationFragment;
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
                onStateChange(AppState.PEOPLE);
                actionBar.setTitle(R.string.title_people);
                break;
            case AppConstants.SECTION_NOTIFICATIONS:
                if(notificationFragment == null) notificationFragment = NotificationFragment.newInstance();
                newFragment = notificationFragment;
                onStateChange(AppState.NOTIFICATIONS);
                if(actionBar != null) actionBar.setTitle(R.string.title_notifications);
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
                        //.commitAllowingStateLoss();
            }
        }

        //System.gc();
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
                showSection(AppConstants.SECTION_LOGIN);
                onStateChange(AppState.LOGIN);
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
        Intent haystackIntent = new Intent(activity, HaystackActivity.class);
        haystackIntent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) haystack);
        activity.startActivity(haystackIntent);
    }

    public void refreshLocationSharingList() {
        locationSharingListFragment.fetchLocationSharing(true);
    }

    public void onLogOutComplete() {
        Log.i(TAG, "Log out complete");

        Needle.userModel.setLoggedIn(false);

        activity.getDrawerToggle().setDrawerIndicatorEnabled(false);
        activity.getDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_logo_24dp);
        activity.getDrawerToggle().syncState();

        setCurrentState(AppState.LOGIN);

        activity.finish();
        activity.startActivity(new Intent(activity.getBaseContext(), AuthenticationActivity.class));
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
            case AppState.PUBLIC_HAYSTACK_TAB:
            case AppState.PRIVATE_HAYSTACK_TAB:
                return haystacksListFragment;
            case AppState.SETTINGS:
                return settingsFragment;
            case AppState.NOTIFICATIONS:
                return notificationFragment;
        }

        return null;
    }

    public void setActionBarTitle(String title){
        actionBar.setTitle(title);
    }

    public void setPreviousState(int previousState) {
        this.previousState = previousState;
    }

    public NavigationView.OnNavigationItemSelectedListener getNavigationItemListener() {
        return navigationItemListener;
    }
}
