package com.needletest.pafoid.needletest.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.User;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;


public class HaystackActivity extends ActionBarActivity
        implements HaystackNavigationDrawerFragment.NavigationDrawerCallbacks{

    private static final String TAG = "HaystackActivity";

    private HaystackNavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private Haystack haystack;
    private int userId = -1;
    private boolean isOwner;

    private HaystackUserListFragment haystackUserListFragment;
    public CustomSupportMapFragment mMapFragment;

    //Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            haystack = getIntent().getExtras().getParcelable(AppConstants.HAYSTACK_DATA_KEY);
        }else{
            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haystack);

        //Navigation Drawer
        mNavigationDrawerFragment = (HaystackNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.haystack_navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.haystack_navigation_drawer, (DrawerLayout) findViewById(R.id.haystack_layout));

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_users);
        isOwner = haystack.getOwner() == getUserId();
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position){
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.haystack_fragment_container, getHaystackMapFragment())
                        .commit();
                break;
            case 1:
                //User list
                fragmentManager.beginTransaction()
                        .replace(R.id.haystack_fragment_container, getHaystackUserListFragment())
                        .commit();

                break;
            case 2:
            //Share location
                toggleLocationSharing();
                break;
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            getMenuInflater().inflate(R.menu.haystack, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.location_sharing:
                toggleLocationSharing();
                item.setIcon(mMapFragment.isPostingLocationUpdates() ?
                        getResources().getDrawable(R.drawable.ic_action_location_found) :
                        getResources().getDrawable(R.drawable.ic_action_location_off));
                return true;
            case R.id.add_pin:
                //addPin();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Actions
    private void toggleLocationSharing(){
        mMapFragment.toggleLocationSharing();
        mNavigationDrawerFragment.setIsSharingLocation(mMapFragment.isPostingLocationUpdates());
    }

    private void addUsers(){
        Intent intent = new Intent(this, HaystackUserActivity.class);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.ADD_USERS);
        intent.putExtra(AppConstants.TAG_HAYSTACK_ID, haystack.getId());
        startActivityForResult(intent, HaystackUserActivity.ADD_USERS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == HaystackUserActivity.ADD_USERS) {
            ArrayList<User> addedUsers = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
            haystack.getUsers().addAll(addedUsers);
        }
    }

    //Getters/Setters
    public CustomSupportMapFragment getHaystackMapFragment() {
        if(mMapFragment==null){
            mMapFragment = CustomSupportMapFragment.newInstance();
            mMapFragment.setRetainInstance(true);
        }
        mTitle = haystack.getName();
        restoreActionBar();

        return mMapFragment;
    }

    public HaystackUserListFragment getHaystackUserListFragment() {
        if(haystackUserListFragment==null){
            haystackUserListFragment = HaystackUserListFragment.newInstance(haystack);
        }
        mTitle = haystack.getName();
        restoreActionBar();

        return haystackUserListFragment;
    }

    public Haystack getHaystack() {
        return haystack;
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

}
