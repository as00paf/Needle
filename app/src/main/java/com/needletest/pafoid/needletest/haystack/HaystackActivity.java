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
        implements HaystackNavigationDrawerFragment.NavigationDrawerCallbacks, HaystackMapFragment.OnFragmentInteractionListener,
        HaystackUserListFragment.OnFragmentInteractionListener{

    private static final String TAG = "HaystackActivity";

    private HaystackNavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private Haystack haystack;

    private HaystackMapFragment haystackMapFragment;
    private HaystackUserListFragment haystackUserListFragment;

    //Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haystack);

        mNavigationDrawerFragment = (HaystackNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        onNavigationDrawerItemSelected(0);

        haystack = (Haystack) getIntent().getExtras().getParcelable(AppConstants.HAYSTACK_DATA_KEY);
        mTitle = haystack.getName();
    }

    @Override
    public void onResume(){
        super.onResume();
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
                if(getHaystackMapFragment().isPostingLocationUpdates()){
                    getHaystackMapFragment().stopSharingLocation();
                   mNavigationDrawerFragment.setLocationLabel(getResources().getString(R.string.shareLocation));
                }else{
                    getHaystackMapFragment().shareLocation();
                    mNavigationDrawerFragment.setLocationLabel(getResources().getString(R.string.stopSharingLocation));
                }

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
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.haystack, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public HaystackMapFragment getHaystackMapFragment() {
        if(haystackMapFragment==null){
            haystackMapFragment = HaystackMapFragment.newInstance();
        }

        return haystackMapFragment;
    }

    public HaystackUserListFragment getHaystackUserListFragment() {
        if(haystackUserListFragment==null){
            haystackUserListFragment = HaystackUserListFragment.newInstance(haystack);
            mTitle = haystack.getName();
            restoreActionBar();
        }

        return haystackUserListFragment;
    }
}
