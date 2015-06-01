package com.nemator.needle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.haystacks.HaystackListFragment;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackMapFragment;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackUsersFragment;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.locationSharing.LocationSharingFragment;
import com.nemator.needle.view.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

public class MainActivity extends MaterialNavigationDrawer implements LoginTask.LoginResponseHandler, OnActivityStateChangeListener, MaterialSectionListener {
    public static String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;
    private int currentState = AppState.LOGIN;
    private int previousState;

    private int userId = -1;
    private String userName;
    public boolean autoLogin = true;
    public boolean loggedIn = false;

    //Sections/Fragments
    private MaterialSection loginSection;
    private LoginFragment loginFragment;

    private MaterialSection haystacksSection;
    private HaystackListFragment haystacksListFragment;

    private MaterialSection locationSharingSection;
    private LocationSharingFragment locationSharingFragment;

    private MaterialSection logOutSection;

    @Override
    public void init(Bundle savedInstanceState) {
        //Auto-Login
        if(savedInstanceState != null){
            autoLogin = savedInstanceState.getBoolean("autoLogin", true);
        }

        if(getIntent().getExtras() != null){
            autoLogin = getIntent().getExtras().getBoolean("autoLogin");
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        String username = mSharedPreferences.getString("username", "");
        MaterialAccount account;
        if(!username.isEmpty()){
            String email = mSharedPreferences.getString("email", "");
            account = new MaterialAccount(getResources(), username, email, R.drawable.me, R.drawable.mat);
            this.setFirstAccountPhoto(getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
        }else{
            account = new MaterialAccount(getResources(), "Please Log In", "", R.drawable.ic_action_person, R.drawable.mat);
        }

        this.addAccount(account);

        // create sections
        loginFragment = new LoginFragment();
        loginSection = newSection(getString(R.string.login), R.drawable.ic_account, loginFragment);
        this.addSection(loginSection);
        this.addBottomSection(newSection(getString(R.string.title_settings), R.drawable.ic_action_settings, new SettingsFragment()));
        this.addBottomSection(newSection(getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));

        haystacksListFragment = new HaystackListFragment();

        //Navigation Drawer
        Boolean firstNavDrawerLearned = mSharedPreferences.getBoolean("firstNavDrawerLearned", false);

        if(firstNavDrawerLearned){
            this.disableLearningPattern();
        }else{
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }
    }

    @Override
    public void onBackPressed() {
        switch (currentState){
            case AppState.LOGIN:
                super.onBackPressed();
                break;
            case AppState.REGISTER:
                super.onBackPressed();//TODO : Handle this
                break;
            case AppState.PUBLIC_HAYSTACK_TAB:
                logOut();
                break;
            case AppState.PRIVATE_HAYSTACK_TAB:
                //Goto Public tab
                haystacksListFragment.goToTab(0);
                break;
            case AppState.CREATE_HAYSTACK_GENERAL_INFOS:
                //Goto Haystack List
                showHaystacksFragment();
                break;
            case AppState.CREATE_HAYSTACK_MAP:
                //Goto General Infos
                ((CreateHaystackFragment) getSupportFragmentManager().getFragments().get(0)).goToPage(0);
                break;
            case AppState.CREATE_HAYSTACK_USERS:
                //Goto Map
                ((CreateHaystackFragment) getSupportFragmentManager().getFragments().get(0)).goToPage(1);
                break;
            case AppState.CREATE_HAYSTACK_MAP_SEARCH_OPEN:
                //Close Search
                ((CreateHaystackMapFragment) getSupportFragmentManager().getFragments().get(4)).closeSearchResults();
                break;
            case AppState.CREATE_HAYSTACK_USERS_SEARCH_OPEN:
                //Close Search
                ((CreateHaystackUsersFragment) getSupportFragmentManager().getFragments().get(5)).closeSearchResults();
                break;
            case AppState.LOCATION_SHARING_RECEIVED_TAB:
                showHaystacksFragment();
                break;
            case AppState.LOCATION_SHARING_SENT_TAB:
                //Goto Received Tab
                locationSharingFragment.goToPage(0);
                break;
            case AppState.CREATE_LOCATION_SHARING:
                showLocationSharingFragment();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    @Override
    public void onStateChange(int state) {
        previousState = currentState;
        currentState = state;
    }

    @Override
    public int getCurrentState() {
        return currentState;
    }

    @Override
    public int getPreviousState() {
        return previousState;
    }

    @Override
    public void onClick(MaterialSection section) {
        super.onClick(section);
    }

    private void logOut(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.log_out))
                .setMessage(getString(R.string.log_out_confirmation_msg))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLoginFragment(true);
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    //Handlers
    public void onLoginComplete(AuthenticationResult result){
        if (!loggedIn){
            if(result.successCode == 1){
                loggedIn = true;

                //Add/Remove Sections
                this.removeSection(loginSection);

                haystacksSection = newSection(getString(R.string.title_haystacks), R.drawable.ic_haystack, haystacksListFragment);
                this.addSection(haystacksSection);

                locationSharingFragment = new LocationSharingFragment();
                locationSharingSection = newSection(getString(R.string.title_location_sharing), R.drawable.ic_action_location_found, locationSharingFragment);
                this.addSection(locationSharingSection);
                this.addDivisor();

                logOutSection = newSection(getString(R.string.title_logOut), R.drawable.ic_action_exit, this);
                this.addSection(logOutSection);
                this.addDivisor();

                //Account
                String username = mSharedPreferences.getString("username", "");
                String email = mSharedPreferences.getString("email", "");

                MaterialAccount account = new MaterialAccount(getResources(), username, email, R.drawable.me, R.drawable.mat);
                this.setFirstAccountPhoto(getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
                this.addAccount(account);

                showHaystacksFragment();
            }else {
                Toast.makeText(this, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.e(TAG, "onLoginComplete::who called me !?!");
        }
    }

    private void showLoginFragment(Boolean updateSections){
        if(updateSections){
            this.removeSection(haystacksSection);
            this.removeSection(locationSharingSection);
            this.removeSection(locationSharingSection);

            this.addSection(loginSection);
        }
        this.setFragment(loginFragment, getString(R.string.login));
        onStateChange(AppState.LOGIN);
    }

    private void showHaystacksFragment(){
        this.setFragment(haystacksListFragment, getString(R.string.title_activity_haystacks));
        onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
    }

    private void showLocationSharingFragment(){
        this.setFragment(locationSharingFragment, getString(R.string.title_location_sharing));
        onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
    }

    //Getters/Setters
    public int getUserId(){
        if(userId==-1){
            userId = mSharedPreferences.getInt("userId", -1);
        }

        return userId;
    }

    public String getUserName(){
        if(userName == null){
            userName = mSharedPreferences.getString("username", "username");
        }

        return userName;
    }

    public void setHaystacksCount(int count){
        haystacksSection.setNotifications(count);
    }

    public void setLocationSharingCount(int count){
        locationSharingSection.setNotifications(count);
    }
}
