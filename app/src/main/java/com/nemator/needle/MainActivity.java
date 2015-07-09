package com.nemator.needle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.controller.GCMController;
import com.nemator.needle.controller.NavigationController;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

public class MainActivity extends MaterialNavigationDrawer implements MaterialSectionListener {

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

    @Override
    public void init(Bundle savedInstanceState) {
        userModel = new UserModel(this);
        navigationController = new NavigationController(this, userModel);
        authenticationController = new AuthenticationController(this, userModel, navigationController);
        gcmController = new GCMController(this, userModel);

        //Saved Instance State
        if(savedInstanceState != null){
            userModel.setAutoLogin(savedInstanceState.getBoolean("autoLogin", true));
            userModel.setLoggedIn(savedInstanceState.getBoolean("loggedIn", false));
            navigationController.setCurrentState(savedInstanceState.getInt(AppConstants.APP_STATE, AppState.PUBLIC_HAYSTACK_TAB));
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        if(!userModel.isLoggedIn()){
            String username = mSharedPreferences.getString("username", "");
            if(!username.isEmpty()){
                authenticationController.setAccount();
            }

            navigationController.addSection(AppConstants.SECTION_LOGIN);
            navigationController.addSection(AppConstants.SECTION_REGISTER);

            navigationController.showSection(AppConstants.SECTION_LOGIN);
        }else{
            //Add/Remove Sections
            navigationController.removeSection(AppConstants.SECTION_LOGIN);
            navigationController.removeSection(AppConstants.SECTION_REGISTER);

            navigationController.addSection(AppConstants.SECTION_HAYSTACKS);
            navigationController.addSection(AppConstants.SECTION_LOCATION_SHARING);
            navigationController.addSection(AppConstants.SECTION_LOG_OUT);

            navigationController.showSection(AppConstants.SECTION_HAYSTACKS);
        }

        // create sections
        navigationController.addSection(AppConstants.SECTION_SETTINGS);
        navigationController.addSection(AppConstants.SECTION_HELP);

        //Navigation Drawer
        Boolean firstNavDrawerLearned = mSharedPreferences.getBoolean("firstNavDrawerLearned", false);

        if(firstNavDrawerLearned){
            this.disableLearningPattern();
        }else{
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }

        //Needle Location Service
        mConnection = new ServiceConnection(){
            public void onServiceConnected(ComponentName className, IBinder service) {
                locationService = ((NeedleLocationService.LocalBinder)service).getService();
            }

            public void onServiceDisconnected(ComponentName className) {
                locationService = null;
            }
        };

        Intent serviceIntent = new Intent(this, NeedleLocationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(navigationController.getHaystackFragment() != null){
            savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, navigationController.getHaystackFragment().getHaystack());
            savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER, navigationController.getHaystackFragment().isOwner());
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
        navigationController.onBackPressed();
    }

    //Material NavDrawer Listener
    @Override
    public void onClick(MaterialSection section) {
        if(section.getTitle().equals(getString(R.string.title_logOut))){
            this.closeDrawer();
            navigationController.onClickSection(section);
        } else if(section.getTitle().equals(getString(R.string.title_settings))){
            navigationController.setCurrentState(AppState.SETTINGS);
            super.onClick(section);
        }else{
            super.onClick(section);
        }
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

    public interface NavigationHandler{
        void onBackPressed();
        boolean onCreateOptionsMenu(Menu menu);
        boolean onOptionsItemSelected(MenuItem item);
        Menu getMenu();
        void onClickSection(MaterialSection section);
    }
}