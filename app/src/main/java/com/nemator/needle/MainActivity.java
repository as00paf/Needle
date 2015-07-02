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
import android.widget.Toast;

import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.tasks.createHaystack.CreateHaystackResult;
import com.nemator.needle.tasks.createHaystack.CreateHaystackTask;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingResult;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTask;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTask.CreateLocationSharingResponseHandler;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.haystack.HaystackFragment;
import com.nemator.needle.view.haystacks.HaystackListFragment;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.locationSharing.LocationSharingFragment;
import com.nemator.needle.view.locationSharing.createLocationSharing.CreateLocationSharingFragment;
import com.nemator.needle.view.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

public class MainActivity extends MaterialNavigationDrawer implements LoginTask.LoginResponseHandler,
        OnActivityStateChangeListener, MaterialSectionListener, HaystackListFragment.HaystackListFragmentInteractionListener,
        LocationSharingFragment.LocationSharingFragmentInteractionListener, CreateHaystackTask.CreateHaystackResponseHandler, CreateLocationSharingResponseHandler {
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

    private CreateHaystackFragment createHaystackFragment;

    private MaterialSection haystackSection;
    private HaystackFragment haystackFragment;

    private MaterialSection logOutSection;
    private CreateLocationSharingFragment createLocationSharingFragment;

    private Menu menu;

    //Service
    private ServiceConnection mConnection;
private NeedleLocationService locationService;

    @Override
    public void init(Bundle savedInstanceState) {
        //Saved Instance State
        if(savedInstanceState != null){
            autoLogin = savedInstanceState.getBoolean("autoLogin", true);
            currentState = savedInstanceState.getInt(AppConstants.APP_STATE, AppState.PUBLIC_HAYSTACK_TAB);
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

        //Service
        mConnection = new ServiceConnection(){
            public void onServiceConnected(ComponentName className, IBinder service) {
                locationService = ((NeedleLocationService.LocalBinder)service).getService();
            }

            public void onServiceDisconnected(ComponentName className) {
                locationService = null;
            }
        };

        bindService(new Intent(this,
                NeedleLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(haystackFragment != null){
            savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystackFragment.getHaystack());
            savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER, haystackFragment.isOwner());
        }

        savedInstanceState.putInt(AppConstants.APP_STATE, currentState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        if (!isDrawerOpen()) {
            if(getCurrentState() == AppState.HAYSTACK){
                getMenuInflater().inflate(R.menu.haystack, menu);
            }else{
                getMenuInflater().inflate(R.menu.menu_haystacks, menu);
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
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
                locationSharingFragment.goToPage(0);
                break;
            case AppState.CREATE_LOCATION_SHARING:
                showLocationSharingFragment();
                break;
            case AppState.HAYSTACK:
                removeHaystackFragment();
                showHaystacksFragment();
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

    private void removeHaystackFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(haystackFragment);
        trans.commit();

        this.removeSection(haystackSection);
        haystackSection = null;
        haystackFragment = null;
    }

    private void removeCreateLocationSharingFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(createHaystackFragment);
        trans.commit();
        createHaystackFragment = null;
    }

    @Override
    public void onRefreshLocationSharingList() {
        locationSharingFragment.fetchLocationSharing();
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
                        removeSection(logOutSection);
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    //Handlers
    public void onLoginComplete(AuthenticationResult result){
        if(loggedIn == false){
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
        }
    }

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

    @Override
    public void onCreateHaystackFabTapped() {
        showCreateHaystackFragment();
    }

    @Override
    public void onCreateLocationSharingFabTapped() {
        showCreateLocationSharingFragment();
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

    @Override
    public void onLocationSharingCreated(CreateLocationSharingResult result) {
        if(result.successCode == 1){
            locationService.addPostLocationRequest(LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_LOCATION_SHARING, result.locationSharing.getTimeLimit(), result.locationSharing.getId());
            Toast.makeText(this, "Location shared with " + result.locationSharing.getReceiverName(), Toast.LENGTH_SHORT).show();
            showLocationSharingFragment();
            removeCreateLocationSharingFragment();
        }else{
            Toast.makeText(this, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
        }
    }
}
