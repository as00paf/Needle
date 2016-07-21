package com.nemator.needle.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.fragments.needle.NeedleListFragment;
import com.nemator.needle.fragments.needle.createNeedle.CreateNeedleExpirationFragment;
import com.nemator.needle.fragments.notifications.NotificationFragment;
import com.nemator.needle.fragments.people.PeopleFragment;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationController implements HomeActivity.NavigationHandler, OnActivityStateChangeListener{

    private static final String TAG = "NavigationController";

    private static NavigationController instance;

    private HomeActivity activity;

    //Fragments
    private HaystackListFragment haystacksListFragment;
    private NeedleListFragment needleListFragment;
    private CreateNeedleExpirationFragment createNeedleExpirationFragment;
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
                case R.id.drawer_needle:
                    showSection(AppConstants.SECTION_NEEDLES);
                    break;
                case R.id.drawer_notifications:
                    showSection(AppConstants.SECTION_NOTIFICATIONS);
                    break;
                case R.id.drawer_log_out:
                    logOut();
                    return true;
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
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                Intent intent = new Intent(activity, CreateHaystackActivity.class);
                activity.startActivity(intent);
                return;
            case AppConstants.SECTION_NEEDLES:
                if(needleListFragment == null) needleListFragment = NeedleListFragment.newInstance();
                /*if(needleListFragment == null) */needleListFragment = NeedleListFragment.newInstance();
                newFragment = needleListFragment;
                onStateChange(AppState.NEEDLE_RECEIVED_TAB);
                actionBar.setTitle(R.string.title_needles);
                break;
            case AppConstants.SECTION_CREATE_NEEDLE:
                if(createNeedleExpirationFragment == null) createNeedleExpirationFragment = new CreateNeedleExpirationFragment();
                newFragment = createNeedleExpirationFragment;
                onStateChange(AppState.NEEDLE_RECEIVED_TAB);
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
            case AppConstants.SECTION_USER_PROFILE:
                Log.d(TAG, "do something here");
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
            case AppState.NEEDLE_RECEIVED_TAB:
                showSection(AppConstants.SECTION_HAYSTACKS);
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.NEEDLE_SENT_TAB:
                //Goto Received Tab
                needleListFragment.goToPage(0);
                onStateChange(AppState.NEEDLE_RECEIVED_TAB);
                break;
            case AppState.CREATE_NEEDLE:
                showSection(AppConstants.SECTION_NEEDLES);
                needleListFragment.goToPage(0);
                onStateChange(AppState.NEEDLE_RECEIVED_TAB);
                break;
            case AppState.HAYSTACK:
                //removeSection(AppConstants.SECTION_HAYSTACK);
                showSection(AppConstants.SECTION_HAYSTACKS);
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.NEEDLE:
                showSection(AppConstants.SECTION_NEEDLES);
                //removeSection(AppConstants.SECTION_LOCATION_SHARING);
                onStateChange(AppState.NEEDLE_RECEIVED_TAB);
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
        }else if(state != AppState.LOGIN && state != AppState.SPLASH_LOGIN && state != AppState.REGISTER){
            activity.getMenuInflater().inflate(R.menu.menu_haystacks, menu);
        }

        return true;
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

    public void refreshNeedleList() {
        if(needleListFragment != null){
            needleListFragment.fetchNeedles(true);
        }
    }

    public void refreshHaystackList() {
        if(haystacksListFragment != null){
            haystacksListFragment.fetchHaystacks(true);
        }
    }

    public void refreshNotificationList() {
        if(notificationFragment != null){
            notificationFragment.fetchNotifications();
        }
    }

    public void refreshFriendsList() {
        if(peopleFragment != null){
            peopleFragment.fetchFriends(true);
        }
    }

    public void onLogOutComplete() {
        Log.i(TAG, "Log out complete");

        Needle.userModel.setLoggedIn(false);

        activity.getDrawerToggle().setDrawerIndicatorEnabled(false);
        activity.getDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_logo_24dp);
        activity.getDrawerToggle().syncState();

        setCurrentState(AppState.LOGIN);

        activity.finish();
        Intent intent = new Intent(activity.getBaseContext(), AuthenticationActivity.class);
        intent.setAction(AppConstants.LOG_OUT);
        activity.startActivity(intent);
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
