package com.nemator.needle.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetworkManager;
import com.nemator.needle.MainActivity;
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
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.authentication.RegisterFragment;
import com.nemator.needle.view.haystack.HaystackFragment;
import com.nemator.needle.view.haystacks.HaystackListFragment;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.view.haystacks.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.locationSharing.LocationSharingListFragment;
import com.nemator.needle.view.locationSharing.createLocationSharing.CreateLocationSharingFragment;
import com.nemator.needle.view.locationSharing.locationSharing.LocationSharingFragment;
import com.nemator.needle.view.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.elements.MaterialSection;

import static com.nemator.needle.tasks.locationSharing.LocationSharingTask.CreateLocationSharingResponseHandler;
import static com.nemator.needle.view.authentication.LoginFragment.LoginFragmentInteractionListener;
import static com.nemator.needle.view.haystacks.HaystackListFragment.HaystackListFragmentInteractionListener;
import static com.nemator.needle.view.locationSharing.LocationSharingListFragment.LocationSharingListFragmentInteractionListener;

public class NavigationController implements MainActivity.NavigationHandler, OnActivityStateChangeListener,
        HaystackListFragmentInteractionListener, LocationSharingListFragmentInteractionListener,
        CreateHaystackResponseHandler, LoginFragmentInteractionListener, LogOutResponseHandler, CreateLocationSharingResponseHandler {

    private static final String TAG = "NavigationController";

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";

    private MainActivity activity;
    private UserModel userModel;

    //Sections/Fragments
    private MaterialSection loginSection;
    private LoginFragment loginFragment;

    private MaterialSection registerSection;
    private RegisterFragment registerFragment;

    private MaterialSection haystacksSection;
    private HaystackListFragment haystacksListFragment;

    private MaterialSection locationSharingListSection;
    private LocationSharingListFragment locationSharingListFragment;

    private CreateHaystackFragment createHaystackFragment;

    private MaterialSection settingsSection;
    private SettingsFragment settingsFragment;

    private MaterialSection haystackSection;
    private HaystackFragment haystackFragment;

    private MaterialSection logOutSection;
    private CreateLocationSharingFragment createLocationSharingFragment;

    private MaterialSection locationSharingSection;
    private LocationSharingFragment locationSharingFragment;

    private Menu menu;

    private int currentState = AppState.LOGIN;
    private int previousState;

    private static ProgressDialog pd;
    static Context context;

    public NavigationController(MainActivity activity, UserModel userModel){
        this.activity = activity;
        this.context = activity;
        this.userModel = userModel;
    }

    public void addSection(int type){
        switch(type){
            case AppConstants.SECTION_LOGIN :
                loginFragment = new LoginFragment();
                loginSection = activity.newSection(activity.getString(R.string.login), R.drawable.ic_account, loginFragment);
                activity.addSection(loginSection);
                break;
            case AppConstants.SECTION_REGISTER :
                registerFragment = new RegisterFragment();
                registerSection = activity.newSection(activity.getString(R.string.register), R.drawable.ic_account, registerFragment);
                activity.addSection(registerSection);
                break;
            case AppConstants.SECTION_HAYSTACKS :
                haystacksListFragment = new HaystackListFragment();
                haystacksSection = activity.newSection(activity.getString(R.string.title_haystacks), R.drawable.ic_haystack, haystacksListFragment);
                activity.addSection(haystacksSection);
                break;
            case AppConstants.SECTION_LOCATION_SHARING_LIST :
                locationSharingListFragment = new LocationSharingListFragment();
                locationSharingListSection = activity.newSection(activity.getString(R.string.title_location_sharing), R.drawable.ic_action_location_found, locationSharingListFragment);
                activity.addSection(locationSharingListSection);
                break;
            case AppConstants.SECTION_LOG_OUT :
                logOutSection = activity.newSection(activity.getString(R.string.title_logOut), R.drawable.ic_action_exit, this);
                activity.addSection(logOutSection);
                break;
            case AppConstants.SECTION_SETTINGS :
                settingsFragment = new SettingsFragment();
                settingsSection = activity.newSection(activity.getString(R.string.title_settings), R.drawable.ic_action_settings, settingsFragment);
                activity.addBottomSection(settingsSection);
                break;
            case AppConstants.SECTION_HELP :
                //TODO:Use real help fragment
                activity.addBottomSection(activity.newSection(activity.getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));
                break;
        }
    }

    public void createMainSections(){
        addSection(AppConstants.SECTION_HAYSTACKS);
        addSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
        addSection(AppConstants.SECTION_LOG_OUT);
    }

    public void showSection(int type){
        switch(type) {
            case AppConstants.SECTION_REGISTER:
                activity.setSection(registerSection);
                activity.setFragment(registerFragment, activity.getString(R.string.register));
                onStateChange(AppState.REGISTER);
                break;
            case AppConstants.SECTION_LOGIN:
                activity.setSection(loginSection);
                activity.setFragment(loginFragment, activity.getString(R.string.login));
                onStateChange(AppState.LOGIN);
                break;
            case AppConstants.SECTION_HAYSTACKS:
                activity.setSection(haystacksSection);
                activity.setFragment(haystacksListFragment, activity.getString(R.string.title_haystacks));
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppConstants.SECTION_LOCATION_SHARING_LIST:
                activity.setSection(locationSharingListSection);
                activity.setFragment(locationSharingListFragment, activity.getString(R.string.title_location_sharing));
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                if(createHaystackFragment == null) createHaystackFragment = new CreateHaystackFragment();
                activity.setFragment(createHaystackFragment, activity.getString(R.string.create_haystack));
                onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                break;
            case AppConstants.SECTION_CREATE_LOCATION_SHARING:
                if(createLocationSharingFragment == null) createLocationSharingFragment = new CreateLocationSharingFragment();
                activity.setFragment(createLocationSharingFragment, activity.getString(R.string.create_location_sharing));
                onStateChange(AppState.CREATE_LOCATION_SHARING);
                break;
            case AppConstants.SECTION_SETTINGS:
                if(settingsFragment == null) settingsFragment = new SettingsFragment();
                activity.setFragment(settingsFragment, activity.getString(R.string.title_settings));
                onStateChange(AppState.SETTINGS);
                break;
            case AppConstants.SECTION_LOCATION_SHARING:
                if(locationSharingFragment == null) locationSharingFragment = new LocationSharingFragment();
                activity.setFragment(locationSharingFragment, activity.getString(R.string.title_location_sharing));
                onStateChange(AppState.LOCATION_SHARING);
                break;
            case AppConstants.SECTION_HAYSTACK:
                if(haystackFragment == null) haystackFragment = new HaystackFragment();
                activity.setFragment(haystackFragment, activity.getString(R.string.title_activity_haystack));
                onStateChange(AppState.HAYSTACK);
                break;
        }
    }

    public void removeSection(int type){
        switch(type) {
            case AppConstants.SECTION_REGISTER:
                removeFragment(registerFragment);
                registerFragment = null;
                if(registerSection!=null){
                    activity.removeSection(registerSection);
                    registerSection = null;
                }
                break;
            case AppConstants.SECTION_LOGIN:
                removeFragment(loginFragment);
                loginFragment = null;
                if(loginSection!=null){
                    activity.removeSection(loginSection);
                    loginSection = null;
                }
                break;
            case AppConstants.SECTION_HAYSTACKS:
                removeFragment(haystacksListFragment);
                haystacksListFragment = null;
                if(haystacksSection != null){
                    activity.removeSection(haystacksSection);
                    haystacksSection = null;
                }
                break;
            case AppConstants.SECTION_LOCATION_SHARING_LIST:
                removeFragment(locationSharingListFragment);
                locationSharingListFragment = null;
                if(locationSharingListSection != null){
                    activity.removeSection(locationSharingListSection);
                    locationSharingListSection = null;
                }
                break;
            case AppConstants.SECTION_SETTINGS:
                removeFragment(settingsFragment);
                settingsFragment = null;
                activity.removeSection(settingsSection);
                settingsSection = null;
                break;
            case AppConstants.SECTION_LOG_OUT:
                activity.removeSection(logOutSection);
                logOutSection = null;
                break;
            case AppConstants.SECTION_CREATE_HAYSTACK:
                removeFragment(createHaystackFragment);
                createHaystackFragment = null;
                break;
            case AppConstants.SECTION_CREATE_LOCATION_SHARING:
                removeFragment(createLocationSharingFragment);
                createLocationSharingFragment = null;
                break;
            case AppConstants.SECTION_LOCATION_SHARING:
                removeFragment(locationSharingFragment);
                locationSharingFragment = null;
                activity.removeSection(locationSharingSection);
                locationSharingSection = null;
                break;
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
                removeSection(AppConstants.SECTION_CREATE_HAYSTACK);
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
                removeSection(AppConstants.SECTION_HAYSTACK);
                showSection(AppConstants.SECTION_HAYSTACKS);
                haystacksListFragment.goToTab(0);
                onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                break;
            case AppState.LOCATION_SHARING:
                showSection(AppConstants.SECTION_LOCATION_SHARING_LIST);
                removeSection(AppConstants.SECTION_LOCATION_SHARING);
                onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                break;
            case AppState.SETTINGS:
                removeFragment(settingsFragment);
                settingsSection.unSelect();
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

        if (!activity.isDrawerOpen()) {
            //TODO: use switch statement
            if(getCurrentState() == AppState.HAYSTACK){
                activity.getMenuInflater().inflate(R.menu.haystack, menu);
            }else if(getCurrentState() == AppState.SETTINGS){
                activity.getMenuInflater().inflate(R.menu.menu_settings, menu);
            }else{
                activity.getMenuInflater().inflate(R.menu.menu_haystacks, menu);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
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

    @Override
    public void onClickSection(MaterialSection section) {
        if(section == logOutSection){
            logOut();
        }
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
                        ((MainActivity) activity).getAuthenticationController().logOut();
                        new LogOutTask(activity, NavigationController.this).execute();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Deselect logOut section
                        logOutSection.unSelect();
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
        haystackSection = activity.newSection(haystack.getName(), R.drawable.ic_haystack, haystackFragment);
        activity.addSectionAt(haystackSection, 1);

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
        locationSharingSection = activity.newSection(name, R.drawable.ic_action_location_found, locationSharingFragment);
        activity.addSectionAt(locationSharingSection, 2);

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
            haystackSection = activity.newSection(result.haystack.getName(), R.drawable.ic_haystack, haystackFragment);
            activity.addSectionAt(haystackSection, 1);

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
            removeSection(AppConstants.SECTION_CREATE_LOCATION_SHARING);
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

    public void showFacebookFragment(SocialNetworkManager networkManager){
        activity.setFragment(networkManager, SOCIAL_NETWORK_TAG);
    }

    //Getters/Setters
    public HaystackFragment getHaystackFragment() {
        return haystackFragment;
    }

    public void setHaystacksCount(int count){
        if(count>0)
            haystacksSection.setNotifications(count);
    }

    public void setLocationSharingCount(int count){
        if(count>0)
            locationSharingListSection.setNotifications(count);
    }

    @Override
    public void onLogOutComplete() {
        userModel.setLoggedIn(false);
        userModel.setAutoLogin(false);

        removeSection(AppConstants.SECTION_HAYSTACKS);
        removeSection(AppConstants.SECTION_LOCATION_SHARING_LIST);

        addSection(AppConstants.SECTION_LOGIN);
        addSection(AppConstants.SECTION_REGISTER);

        showSection(AppConstants.SECTION_LOGIN);
        removeSection(AppConstants.SECTION_LOG_OUT);
    }

    public LoginFragment getLoginFragment() {
        return loginFragment;
    }

    public static void showProgress(String message) {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        //pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    public static void hideProgress() {
        pd.dismiss();
    }
}
