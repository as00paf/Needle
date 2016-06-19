package com.nemator.needle.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.CreateHaystackPagerAdapter;
import com.nemator.needle.controller.HaystackController;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackGeneralInfosFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackMapFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackUsersFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;

public class CreateHaystackActivity extends AppCompatActivity implements CreateHaystackGeneralInfosFragment.OnPrivacySettingsUpdatedListener,
        HaystackController.CreateHaystackDelegate {

    public static final String TAG = "CreateHaystackActivity";

    //View
    private FloatingActionButton fab;
    private TabLayout tabs;
    private ProgressDialog progressDialog;

    //Data
    private HaystackVO haystack;
    private ArrayList<UserVO> userList = new ArrayList<UserVO>();

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;
    private Boolean isPublic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Toolbar
        setContentView(R.layout.activity_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_haystack);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ViewPager
        mCreateHaystackPagerAdapter = new CreateHaystackPagerAdapter(getSupportFragmentManager(), this, false);
        createHaystackViewPager = (ViewPager) findViewById(R.id.view_pager);
        createHaystackViewPager.setAdapter(mCreateHaystackPagerAdapter);
        createHaystackViewPager.setOffscreenPageLimit(2);
        createHaystackViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 2 || (position == 1 && isPublic)){
                    fab.setVisibility(View.VISIBLE);
                }else{
                    fab.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Tabs
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(createHaystackViewPager);

        //FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHaystack();
            }
        });
        fab.setVisibility(View.INVISIBLE);

        initializeBroadcastListener();
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received API connected Intent");
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(CreateHaystackActivity.this);
            localBroadcastManager.unregisterReceiver(this);

            initPermissionsAndServices();
        }
    };

    private void initPermissionsAndServices() {
        Needle.googleApiController.checkLocationSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
    }

    private void initializeBroadcastListener() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(apiConnectedReceiver,
                new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int position = createHaystackViewPager.getCurrentItem();
        switch(position){
            case 0:
                super.onCreateOptionsMenu(menu);
                return true;
            case 1:
                if(isPublic){
                    getMenuInflater().inflate(R.menu.menu_create_haystack_done, menu);
                }else{
                    getMenuInflater().inflate(R.menu.menu_create_haystack_map, menu);
                }

                return true;
            case 2:
                getMenuInflater().inflate(R.menu.menu_create_haystack_done, menu);
                return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_done:
                createHaystack();
                return true;
            case android.R.id.home:
                return false;
            case R.id.menu_option_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_option_help:
                startActivity(new Intent(this, HelpSupportActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        CreateHaystackGeneralInfosFragment mCreateHaystackGenInfosFragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackGeneralInfosFragment.class);

        if(mCreateHaystackGenInfosFragment.isCameraShown()){
            mCreateHaystackGenInfosFragment.setCameraShown(false);
            return;
        }

        CreateHaystackMapFragment mCreateHaystackMapFragment = (CreateHaystackMapFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackMapFragment.class);
        if(mCreateHaystackMapFragment.isSearchOpened()){
            mCreateHaystackMapFragment.closeSearchMenu();
            return;
        }

        super.onBackPressed();
    }

    public void createHaystack(){
        //Create VO
        haystack = new HaystackVO();
        CreateHaystackGeneralInfosFragment mCreateHaystackGenInfosFragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackGeneralInfosFragment.class);
        CreateHaystackMapFragment mCreateHaystackMapFragment = (CreateHaystackMapFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackMapFragment.class);
        CreateHaystackUsersFragment mCreateHaystackUsersFragment = (CreateHaystackUsersFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackUsersFragment.class);

        //General Infos
        haystack.setName(mCreateHaystackGenInfosFragment.getHaystackName());
        haystack.setOwner(Needle.userModel.getUserId());
        haystack.setIsPublic(mCreateHaystackGenInfosFragment.getIsPublic());
        haystack.setTimeLimit(mCreateHaystackGenInfosFragment.getDateLimit() + " " + mCreateHaystackGenInfosFragment.getTimeLimit());

        //Location
        haystack.setZoneRadius(mCreateHaystackMapFragment.getZoneRadius());
        haystack.setPosition(mCreateHaystackMapFragment.getPosition());
        haystack.setIsCircle(mCreateHaystackMapFragment.getIsCircle());

        //Users
        if(!userList.contains(Needle.userModel.getUser())){
            userList.add(Needle.userModel.getUser());
        }

        if(mCreateHaystackUsersFragment != null){
            userList.addAll(mCreateHaystackUsersFragment.getSelectedUsers());
        }
        haystack.setUsers(userList);

        //Active users
        ArrayList<UserVO> activeUsers = new ArrayList<UserVO>();
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<UserVO> bannedUsers = new ArrayList<UserVO>();
        haystack.setBannedUsers(bannedUsers);

        if(!validateHaystack(haystack)){
            return;
        }

        progressDialog = ProgressDialog.show(this, getString(R.string.creating_haystack),
                getString(R.string.creating_haystack), true);

        HaystackController.createHaystack(haystack, mCreateHaystackGenInfosFragment.getPicture(), this);
    }

    private Boolean validateHaystack(HaystackVO haystack){
        if(haystack.getName() == null || haystack.getName().isEmpty()){
            if(createHaystackViewPager.getCurrentItem() != 0) createHaystackViewPager.setCurrentItem(0);
            Toast.makeText(this, getString(R.string.haystack_name_error_msg), Toast.LENGTH_SHORT).show();

            return false;
        }

        if(haystack.getUsers().size() < 1) {
            if(createHaystackViewPager.getCurrentItem() != 2) createHaystackViewPager.setCurrentItem(2);
            Toast.makeText(this, getString(R.string.haystack_users_error_msg), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public HaystackVO getHaystack(){
        return haystack;
    }

    @Override
    public void onPrivacySettingsChanged(Boolean isPublic) {
        this.isPublic = isPublic;

        mCreateHaystackPagerAdapter.setIsPublic(isPublic);
        mCreateHaystackPagerAdapter.notifyDataSetChanged();
    }

    //Creation Handler
    @Override
    public void onHaystackCreationSuccess(HaystackVO haystack) {
        progressDialog.dismiss();

        Toast.makeText(CreateHaystackActivity.this, getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

        Intent haystackIntent = new Intent(CreateHaystackActivity.this, HaystackActivity.class);
        haystackIntent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) haystack);
        CreateHaystackActivity.this.startActivity(haystackIntent);

        finish();
    }

    @Override
    public void onHaystackCreationFailed(String result) {
        Log.d(TAG, "An error occured while creating Haystack : " + result);
        progressDialog.dismiss();
        Toast.makeText(this, getString(R.string.haystack_creation_failed_msg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHaystackImageUploadFailed() {
        progressDialog.dismiss();
        Toast.makeText(this, getString(R.string.haystack_image_upload_failed_msg), Toast.LENGTH_SHORT).show();
    }
}
