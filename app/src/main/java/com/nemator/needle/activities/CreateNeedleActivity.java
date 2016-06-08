package com.nemator.needle.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.nemator.needle.views.SlidingTabLayout;

public class CreateNeedleActivity extends AppCompatActivity implements NeedleController.CreateNeedleDelegate {

    public static final String TAG = "CreateLocSharing";

    //View
    private FloatingActionButton fab;
    private SlidingTabLayout tabs;
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

        //Toolbar
        setContentView(R.layout.activity_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ViewPager
        pagerAdapter = new CreateNeedlePagerAdapter(getSupportFragmentManager(), this, userAlreadySelected);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        //Tabs
        tabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        if(!userAlreadySelected){
            tabs.setCustomTabView(R.layout.layout_tab_title, R.id.tab_text);
            tabs.setDistributeEvenly(true);
            tabs.setViewPager(viewPager, toolbar);

            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

                @Override
                public int getIndicatorColor(int position) {
                    return Color.WHITE;
                }

            });
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

        if(!userAlreadySelected){
            getSupportActionBar().setTitle(pagerAdapter.getPageTitle(0));
        }else{
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

        progressDialog = ProgressDialog.show(this, "Sharing Location",
                "Creating Location Sharing with " + selectedUser.getReadableUserName(), true);

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
        Toast.makeText(CreateNeedleActivity.this, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
    }
}
