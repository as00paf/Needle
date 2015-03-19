package com.nemator.needle.home;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.nemator.needle.R;
import com.nemator.needle.settings.AppSettingsActivity;

public class HomeActivity extends ActionBarActivity
        implements HomeNavigationDrawerFragment.NavigationDrawerCallbacks {

    private HomeNavigationDrawerFragment mNavigationDrawerFragment;
    private HaystackListFragment haystackListFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (HomeNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.home_navigation_drawer);
        mTitle = getTitle();

        haystackListFragment = HaystackListFragment.newInstance();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.home_navigation_drawer, (DrawerLayout) findViewById(R.id.home_layout));
        onNavigationDrawerItemSelected(0);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        Intent intent = null;

        switch (position){
            case 0:
                fragment = haystackListFragment;
                break;
            case 1:
                intent = new Intent(this, AppSettingsActivity.class);
                break;
            default:
                fragment = haystackListFragment;
                break;
        }

        if(null != fragment){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.home_fragment_container, fragment)
                    .commit();
        }else if(intent != null){
            startActivity(intent);
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
            getMenuInflater().inflate(R.menu.home, menu);
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
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
