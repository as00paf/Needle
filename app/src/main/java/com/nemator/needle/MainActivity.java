package com.nemator.needle;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.tasks.createHaystack.CreateHaystackResult;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingResult;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTask.CreateLocationSharingResponseHandler;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.haystack.HaystackFragment;
import com.nemator.needle.view.haystacks.HaystackListFragment;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.locationSharing.LocationSharingListFragment;
import com.nemator.needle.view.locationSharing.createLocationSharing.CreateLocationSharingFragment;
import com.nemator.needle.view.locationSharing.locationSharing.LocationSharingMapFragment;
import com.nemator.needle.view.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

import static com.nemator.needle.tasks.createHaystack.CreateHaystackTask.CreateHaystackResponseHandler;
import static com.nemator.needle.tasks.login.LoginTask.LoginResponseHandler;
import static com.nemator.needle.view.haystacks.HaystackListFragment.HaystackListFragmentInteractionListener;
import static com.nemator.needle.view.locationSharing.LocationSharingListFragment.LocationSharingListFragmentInteractionListener;

public class MainActivity extends MaterialNavigationDrawer implements LoginResponseHandler,
        OnActivityStateChangeListener, MaterialSectionListener, HaystackListFragmentInteractionListener,
        LocationSharingListFragmentInteractionListener, CreateHaystackResponseHandler, CreateLocationSharingResponseHandler {
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
    private LocationSharingListFragment locationSharingListFragment;

    private CreateHaystackFragment createHaystackFragment;

    private MaterialSection settingsSection;
    private SettingsFragment settingsFragment;

    private MaterialSection haystackSection;
    private HaystackFragment haystackFragment;

    private MaterialSection logOutSection;
    private CreateLocationSharingFragment createLocationSharingFragment;

    private MaterialSection locationSharingMapSection;
    private LocationSharingMapFragment locationSharingMapFragment;

    private Menu menu;

    //Service
    private ServiceConnection mConnection;
    private NeedleLocationService locationService;

    @Override
    public void init(Bundle savedInstanceState) {
        //Saved Instance State
        if(savedInstanceState != null){
            autoLogin = savedInstanceState.getBoolean("autoLogin", true);
            loggedIn = savedInstanceState.getBoolean("loggedIn", false);
            currentState = savedInstanceState.getInt(AppConstants.APP_STATE, AppState.PUBLIC_HAYSTACK_TAB);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        if(!loggedIn){
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

            loginFragment = new LoginFragment();
            loginSection = newSection(getString(R.string.login), R.drawable.ic_account, loginFragment);
            this.addSection(loginSection);
        }else{
            //Add/Remove Sections
            if(loginSection != null){
                this.removeSection(loginSection);
                loginSection = null;
            }

            loginFragment = null;

            haystacksListFragment = new HaystackListFragment();
            haystacksSection = newSection(getString(R.string.title_haystacks), R.drawable.ic_haystack, haystacksListFragment);
            this.addSection(haystacksSection);

            locationSharingListFragment = new LocationSharingListFragment();
            locationSharingSection = newSection(getString(R.string.title_location_sharing), R.drawable.ic_action_location_found, locationSharingListFragment);
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
        }

        // create sections
        settingsFragment = new SettingsFragment();
        settingsSection = newSection(getString(R.string.title_settings), R.drawable.ic_action_settings, settingsFragment);
        this.addBottomSection(settingsSection);
        this.addBottomSection(newSection(getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));

        //Navigation Drawer
        Boolean firstNavDrawerLearned = mSharedPreferences.getBoolean("firstNavDrawerLearned", false);

        if(firstNavDrawerLearned){
            this.disableLearningPattern();
        }else{
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }

        //Service
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
        if(haystackFragment != null){
            savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystackFragment.getHaystack());
            savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER, haystackFragment.isOwner());
        }

        savedInstanceState.putInt(AppConstants.APP_STATE, currentState);
        savedInstanceState.putBoolean("autoLogin", autoLogin);
        savedInstanceState.putBoolean("loggedIn", loggedIn);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        if (!isDrawerOpen()) {
            //TODO: use switch statement
            if(getCurrentState() == AppState.HAYSTACK){
                getMenuInflater().inflate(R.menu.haystack, menu);
            }else if(getCurrentState() == AppState.SETTINGS){
                getMenuInflater().inflate(R.menu.menu_settings, menu);
            }else{
                getMenuInflater().inflate(R.menu.menu_haystacks, menu);
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_option_settings:
                showSettingsFragment();
                return true;
            case R.id.menu_option_help:

                return true;
        }

        return super.onOptionsItemSelected(item);
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
                createHaystackFragment.goToPage(0);
                break;
            case AppState.CREATE_HAYSTACK_USERS:
                //Goto Map
                createHaystackFragment.goToPage(1);
                break;
            case AppState.CREATE_HAYSTACK_MAP_SEARCH_OPEN:
                //Close Search
                createHaystackFragment.mCreateHaystackMapFragment.closeSearchResults();
                break;
            case AppState.CREATE_HAYSTACK_USERS_SEARCH_OPEN:
                //Close Search
                createHaystackFragment.mCreateHaystackUsersFragment.closeSearchResults();
                break;
            case AppState.LOCATION_SHARING_RECEIVED_TAB:
                showHaystacksFragment();
                break;
            case AppState.LOCATION_SHARING_SENT_TAB:
                //Goto Received Tab
                locationSharingListFragment.goToPage(0);
                break;
            case AppState.CREATE_LOCATION_SHARING:
                showLocationSharingListFragment();
                break;
            case AppState.HAYSTACK:
                removeHaystackFragment();
                showHaystacksFragment();
                break;
            case AppState.LOCATION_SHARING:
                removeLocationSharingMapFragment();
                showLocationSharingListFragment();
                break;
            case AppState.SETTINGS:
                removeSettingsFragment();
                restorePreviousState();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    //AppState Listener
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

    private void restorePreviousState(){
        int state = previousState;
        onStateChange(previousState);

        switch(state){
            case AppState.LOGIN:
                showLoginFragment(true);
                break;
            case AppState.PUBLIC_HAYSTACK_TAB :
            case AppState.PRIVATE_HAYSTACK_TAB :
                showHaystacksFragment();
                break;
            case AppState.LOCATION_SHARING_SENT_TAB :
            case AppState.LOCATION_SHARING_RECEIVED_TAB :
                showLocationSharingListFragment();
                break;
        }
    }

    //Material NavDrawer Listener
    @Override
    public void onClick(MaterialSection section) {
        super.onClick(section);
    }

    //LoginResponseHandler
    public void onLoginComplete(AuthenticationResult result){
        if(loggedIn == false){
            if(result.successCode == 1){
                loggedIn = true;

                //Add/Remove Sections
                this.removeSection(loginSection);
                loginSection = null;
                loginFragment = null;

                haystacksListFragment = new HaystackListFragment();
                haystacksSection = newSection(getString(R.string.title_haystacks), R.drawable.ic_haystack, haystacksListFragment);
                this.addSection(haystacksSection);

                locationSharingListFragment = new LocationSharingListFragment();
                locationSharingSection = newSection(getString(R.string.title_location_sharing), R.drawable.ic_action_location_found, locationSharingListFragment);
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
        }
    }

    //HaystackListFragmentInteractionListener
    @Override
    public void onRefreshHaystackList() {
        haystacksListFragment.fetchHaystacks();
    }

    @Override
    public void onClickHaystackCard(HaystackVO haystack) {
        //Add/Remove Sections
        haystackFragment = new HaystackFragment();
        haystackFragment.setHaystack(haystack);
        haystackSection = newSection(haystack.getName(), R.drawable.ic_haystack, haystackFragment);
        this.addSectionAt(haystackSection, 1);

        showHaystackFragment(haystack.getName());
    }

    @Override
    public void onCreateHaystackFabTapped() {
        showCreateHaystackFragment();
    }

    //LocationSharingListFragmentInteractionListener
    @Override
    public void onCreateLocationSharingFabTapped() {
        showCreateLocationSharingFragment();
    }

    @Override
    public void onClickLocationSharingCard(LocationSharingVO locationSharing, Boolean isSent) {
        //Add/Remove Sections
        locationSharingMapFragment = new LocationSharingMapFragment();
        locationSharingMapFragment.setLocationSharing(locationSharing);
        locationSharingMapFragment.setIsSent(isSent);
        String name = isSent ? locationSharing.getReceiverName() : locationSharing.getSenderName();
        locationSharingMapSection = newSection(name, R.drawable.ic_action_location_found, locationSharingMapFragment);
        this.addSectionAt(locationSharingMapSection, 2);

        showLocationSharingMapFragment(name);
    }

    @Override
    public void onRefreshLocationSharingList() {
        locationSharingListFragment.fetchLocationSharing();
    }

    //CreateHaystackResponseHandler
    @Override
    public void onHaystackCreated(CreateHaystackResult result){
        if(result.successCode == 0){
            Toast.makeText(this, "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }else{
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(createHaystackFragment);
            trans.commit();

            Toast.makeText(this, getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

            //Add/Remove Sections
            haystackFragment = new HaystackFragment();
            haystackFragment.setHaystack(result.haystack);
            haystackSection = newSection(result.haystack.getName(), R.drawable.ic_haystack, haystackFragment);
            this.addSectionAt(haystackSection, 1);

            showHaystackFragment(result.haystack.getName());
        }
    }

    //CreateLocationSharingResponseHandler
    @Override
    public void onLocationSharingCreated(CreateLocationSharingResult result) {
        if(result.successCode == 1){
            locationService.startLocationUpdates();
            locationService.addPostLocationRequest(LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_LOCATION_SHARING, result.locationSharing.getTimeLimit(), result.locationSharing.getId());
            Toast.makeText(this, "Location shared with " + result.locationSharing.getReceiverName(), Toast.LENGTH_SHORT).show();
            showLocationSharingListFragment();
            removeCreateLocationSharingFragment();
        }else{
            Toast.makeText(this, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
        }
    }

    //Fragments Management
    private void showSettingsFragment(){
        if(settingsFragment == null) settingsFragment = new SettingsFragment();
        this.setSection(settingsSection);
        this.setFragment(settingsFragment, getString(R.string.title_settings));
        onStateChange(AppState.SETTINGS);
    }

    private void removeSettingsFragment(){
        if(settingsFragment != null){
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(settingsFragment);
            trans.commit();

            settingsFragment = null;
        }
    }

    private void removeHaystackFragment(){
        if(haystackFragment != null){
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(haystackFragment);
            trans.commit();

            this.removeSection(haystackSection);
            haystackSection = null;
            haystackFragment = null;
        }
    }

    private void removeLocationSharingMapFragment(){
        if(locationSharingMapFragment != null){
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(locationSharingMapFragment);
            trans.commit();

            this.removeSection(locationSharingMapSection);
            locationSharingMapSection = null;
            locationSharingMapFragment = null;
        }
    }


    private void removeCreateLocationSharingFragment(){
        if(createHaystackFragment != null){
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(createHaystackFragment);
            trans.commit();
            createHaystackFragment = null;
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
        this.setSection(haystacksSection);
        this.setFragment(haystacksListFragment, getString(R.string.title_activity_haystacks));
        onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
    }

    private void showLocationSharingListFragment(){
        this.setSection(locationSharingSection);
        this.setFragment(locationSharingListFragment, getString(R.string.title_location_sharing));
        onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
    }

    private void showCreateHaystackFragment(){
        createHaystackFragment = new CreateHaystackFragment();
        this.setFragment(createHaystackFragment, getString(R.string.create_haystack));
        onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
    }

    private void showCreateLocationSharingFragment(){
        createLocationSharingFragment = new CreateLocationSharingFragment();
        this.setFragment(createLocationSharingFragment, getString(R.string.create_location_sharing));
        onStateChange(AppState.CREATE_LOCATION_SHARING);
    }

    private void showHaystackFragment(String title){
        this.setFragment(haystackFragment, title);
        onStateChange(AppState.HAYSTACK);
    }

    private void showLocationSharingMapFragment(String title){
        this.setFragment(locationSharingMapFragment, title);
        onStateChange(AppState.LOCATION_SHARING);
    }

    //Actions
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
                        removeSection(logOutSection);
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
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

    public Menu getMenu(){
        return menu;
    }

    public NeedleLocationService getLocationService() {
        return locationService;
    }
}