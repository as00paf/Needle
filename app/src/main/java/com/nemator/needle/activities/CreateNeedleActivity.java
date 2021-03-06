package com.nemator.needle.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.CreateNeedlePagerAdapter;
import com.nemator.needle.controller.NeedleController;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

public class CreateNeedleActivity extends AppCompatActivity implements NeedleController.CreateNeedleDelegate {

    public static final String TAG = "CreateLocSharing";

    //View
    private FloatingActionButton fab;
    private TabLayout tabs;
    private ViewPager viewPager;
    private ProgressDialog progressDialog;

    private CreateNeedlePagerAdapter pagerAdapter;

    //Data
    private boolean userAlreadySelected;
    private NeedleVO needleVO;
    private UserVO selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedUser = extras.getParcelable(AppConstants.TAG_USER);
            userAlreadySelected = selectedUser != null;
        }

        setContentView(R.layout.activity_create);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_needle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ViewPager
        pagerAdapter = new CreateNeedlePagerAdapter(getSupportFragmentManager(), this, userAlreadySelected);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1){
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
        if(!userAlreadySelected){
            tabs.setTabMode(TabLayout.MODE_FIXED);
            tabs.setTabGravity(TabLayout.GRAVITY_FILL);
            tabs.setupWithViewPager(viewPager);
        }else{
            tabs.setVisibility(View.GONE);
        }

        //FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if(!userAlreadySelected){
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNeedle();
            }
        });

        if(userAlreadySelected){
            getSupportActionBar().setTitle(getString(R.string.send_needle_to, selectedUser.getReadableUserName()));
        }

        initializeBroadcastListener();
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(CreateNeedleActivity.this);
            localBroadcastManager.unregisterReceiver(this);

            initPermissionsAndServices();
        }
    };

    private void initPermissionsAndServices() {
        if(Needle.gcmController.checkPlayServices()){
            Needle.googleApiController.checkLocationSettings();
        }
    }

    private void initializeBroadcastListener() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(apiConnectedReceiver,
                new IntentFilter(AppConstants.GOOGLE_API_CONNECTED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(viewPager.getCurrentItem() == 0){
            getMenuInflater().inflate(R.menu.menu_default, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_create_haystack_done, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_option_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_option_help:
                startActivity(new Intent(this, HelpSupportActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Boolean validate(){
        if(selectedUser == null) return false;
        return true;
    }

    private void createNeedle(){
        if(selectedUser == null){
            selectedUser = pagerAdapter.getSelectedUser();
        }

        if(!validate()) {
            Toast.makeText(this, "Please select a user from the list", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this, getString(R.string.creating_needle),
                getString(R.string.sending_needle_to, selectedUser.getReadableUserName()), true);

        needleVO = new NeedleVO();

        //Sender
        needleVO.setSender(Needle.userModel.getUser());

        //Receiver
        needleVO.setReceiver(selectedUser);

        //Options
        String dateLimit = pagerAdapter.getDateLimit();
        String timeLimit = pagerAdapter.getTimeLimit();

        needleVO.setTimeLimit(dateLimit + " " + timeLimit);

        NeedleController.createNeedle(needleVO, this);
    }

    @Override
    public void onNeedleCreationSuccess(NeedleVO locationSharing) {
        progressDialog.dismiss();

        //Feedback
        Toast.makeText(CreateNeedleActivity.this, "Location shared with " + locationSharing.getReceiver().getReadableUserName(), Toast.LENGTH_SHORT).show();

        //Launch Activity
               /* Intent locationSharingIntent = new Intent(CreateNeedleActivity.this, NeedleActivity.class);
                locationSharingIntent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Parcelable) result.getNeedle());
                startActivity(locationSharingIntent);*/

        finish();
    }

    @Override
    public void onNeedleCreationFailed(String result) {
        progressDialog.dismiss();
        //TODO : use strings
        Toast.makeText(CreateNeedleActivity.this, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
    }
}
