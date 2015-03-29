package com.nemator.needle.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nemator.needle.R;
import com.nemator.needle.authentication.LoginFragment;
import com.nemator.needle.haystack.task.leaveHaystack.LeaveHaystackParams;
import com.nemator.needle.home.HaystackListFragment;
import com.nemator.needle.home.HomeActivity;
import com.nemator.needle.models.Haystack;
import com.nemator.needle.models.TaskResult;
import com.nemator.needle.models.User;
import com.nemator.needle.AppConstants;
import com.nemator.needle.haystack.task.leaveHaystack.LeaveHaystackTask;
import com.nemator.needle.settings.SettingsFragment;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;


public class HaystackActivity extends MaterialNavigationDrawer{

    private static final String TAG = "HaystackActivity";

    private HaystackNavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private Haystack haystack;
    private int userId = -1;
    private boolean isOwner;

    private Menu menu;
    private ImageView directionsArrow;
    private SharedPreferences mSharedPreferences;
    private HaystackMapFragment mMapFragment;
    private HaystackFragment mHaystackFragment;

    @Override
    public void init(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            haystack = getIntent().getExtras().getParcelable(AppConstants.HAYSTACK_DATA_KEY);
        }else{
            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.TAG_IS_OWNER)) {
                isOwner = savedInstanceState.getBoolean(AppConstants.TAG_IS_OWNER);
            }
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        String username = mSharedPreferences.getString("username", "");
        String email = mSharedPreferences.getString("email", "");

        MaterialAccount account = new MaterialAccount(getResources(), username, email, R.drawable.me, R.drawable.mat);
        this.setFirstAccountPhoto(getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
        this.addAccount(account);

        //Create Sections
        mHaystackFragment = new HaystackFragment();
        this.addSection(newSection(haystack.getName(), R.drawable.ic_haystack, mHaystackFragment));
        this.addSection(newSection(getString(R.string.title_haystacks), R.drawable.ic_haystack, new HaystackListFragment()));
        this.addSection(newSection(getString(R.string.title_settings), R.drawable.ic_action_settings, new SettingsFragment()));
        this.addSection(newSection(getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));
        this.addDivisor();
        this.addSection(newSection(getString(R.string.title_logOut), R.drawable.ic_action_exit, this));
        this.addDivisor();

        this.disableLearningPattern();
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            haystack = getIntent().getExtras().getParcelable(AppConstants.HAYSTACK_DATA_KEY);
        }else{
            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.TAG_IS_OWNER)) {
                isOwner = savedInstanceState.getBoolean(AppConstants.TAG_IS_OWNER);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haystack);


        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_users);
        isOwner = isOwner();
        if(isOwner){
            fab.setSize(FloatingActionButton.SIZE_NORMAL);
            fab.setColor(getResources().getColor(R.color.primary));

            fab.initBackground();
            fab.setImageResource(R.drawable.ic_action_add_person);
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addUsers();
                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }

        directionsArrow = (ImageView) findViewById(R.id.directions_arrow);

        RelativeLayout mapFragmentContainer = (RelativeLayout) findViewById(R.id.haystack_activity_container);
        mapFragmentContainer.requestTransparentRegion(findViewById(R.id.haystack_fragment_container));
    }
*/
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        savedInstanceState.putBoolean(AppConstants.TAG_IS_OWNER, isOwner);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

 /*   public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        if (!isDrawerOpen() && getCurrentSection().getTitle().equals(haystack.getName())) {

            getMenuInflater().inflate(R.menu.haystack, menu);
            //restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }


    //Actions
    public void addUsers(){
        Intent intent = new Intent(this, HaystackUserActivity.class);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.ADD_USERS);
        intent.putExtra(AppConstants.TAG_HAYSTACK_ID, haystack.getId());
        startActivityForResult(intent, HaystackUserActivity.ADD_USERS);
    }

    private void getDirections(){
        Intent intent = new Intent(this, HaystackUserActivity.class);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.SELECT_USER_FOR_DIRECTIONS);
        intent.putExtra(AppConstants.TAG_HAYSTACK_ID, haystack.getId());
        startActivityForResult(intent, HaystackUserActivity.SELECT_USER_FOR_DIRECTIONS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if (requestCode == HaystackUserActivity.ADD_USERS) {
                ArrayList<User> addedUsers = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
                haystack.getUsers().addAll(addedUsers);
            }else if(requestCode == HaystackUserActivity.SELECT_USER_FOR_DIRECTIONS){
                ArrayList<User> users = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
                User user = users.get(0);
                getDirectionsToUser(user);
            }
        }
    }

    private void getDirectionsToUser(User user){
        directionsArrow.setVisibility(View.VISIBLE);
    }

    //Getters/Setters
    public HaystackMapFragment getHaystackMapFragment() {
        if(mMapFragment==null){
            mMapFragment = HaystackMapFragment.newInstance();
            mMapFragment.setRetainInstance(true);
        }
        mTitle = haystack.getName();
       // restoreActionBar();

        return mMapFragment;
    }

    /*public HaystackUserListFragment getHaystackUserListFragment() {
        if(haystackUserListFragment==null){
            haystackUserListFragment = HaystackUserListFragment.newInstance(haystack);
        }
        mTitle = haystack.getName();
        //restoreActionBar();

        return haystackUserListFragment;
    }*/

    public Haystack getHaystack() {
        return haystack;
    }

    public int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    public boolean isOwner(){
        int userId = getUserId();
        int ownerId = haystack.getOwner();

        isOwner = userId == ownerId;

        return isOwner;
    }

    public Menu getMenu(){
        return menu;
    }

}
