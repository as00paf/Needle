package com.needletest.pafoid.needletest.haystack;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.models.Haystack;


public class HaystackActivity extends ActionBarActivity
        implements HaystackNavigationDrawerFragment.NavigationDrawerCallbacks,
        HaystackUserListFragment.OnFragmentInteractionListener{

    private static final String TAG = "HaystackActivity";

    private HaystackNavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private Haystack haystack;

    public HaystackMapFragment haystackMapFragment;
    private HaystackUserListFragment haystackUserListFragment;

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

        mNavigationDrawerFragment = (HaystackNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.haystack_navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.haystack_navigation_drawer, (DrawerLayout) findViewById(R.id.haystack_layout));
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
                item.setIcon(haystackMapFragment.mMapFragment.isPostingLocationUpdates() ?
                        getResources().getDrawable(R.drawable.ic_action_location_found) :
                        getResources().getDrawable(R.drawable.ic_action_location_off));
                return true;
            case R.id.add_pin:
                //addPin();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleLocationSharing(){
        haystackMapFragment.mMapFragment.toggleLocationSharing();
        mNavigationDrawerFragment.setIsSharingLocation(haystackMapFragment.mMapFragment.isPostingLocationUpdates());
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public HaystackMapFragment getHaystackMapFragment() {
        if(haystackMapFragment==null){
            haystackMapFragment = HaystackMapFragment.newInstance(haystack);
            haystackMapFragment.setRetainInstance(true);
        }

        return haystackMapFragment;
    }

    public HaystackUserListFragment getHaystackUserListFragment() {
        if(haystackUserListFragment==null){
            haystackUserListFragment = HaystackUserListFragment.newInstance(haystack);
        }
        mTitle = haystack.getName();
        restoreActionBar();

        return haystackUserListFragment;
    }
}
