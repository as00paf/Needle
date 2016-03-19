package com.nemator.needle.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.CreateHaystackPagerAdapter;
import com.nemator.needle.adapter.CreateLocationSharingPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackGeneralInfosFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackMapFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackUsersFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestTask;
import com.nemator.needle.tasks.imageUploader.ImageUploadParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadResult;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.CameraUtils;
import com.nemator.needle.views.SlidingTabLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateLocationSharingActivity extends AppCompatActivity {

    public static final String TAG = "CreateLocSharing";

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_TIME_FORMAT = "HH:mm";

    //View
    private FloatingActionButton fab;
    private SlidingTabLayout tabs;
    private ViewPager viewPager;

    private CreateLocationSharingPagerAdapter pagerAdapter;

    //Data
    private LocationSharingVO locationSharingVO;
    private UserVO selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Toolbar
        setContentView(R.layout.activity_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ViewPager
        pagerAdapter = new CreateLocationSharingPagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        getSupportActionBar().setTitle(pagerAdapter.getPageTitle(0));

        //Tabs
        tabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabs.setCustomTabView(R.layout.layout_tab_title, R.id.tab_text);
        tabs.setDistributeEvenly(true);
        tabs.setViewPager(viewPager, toolbar);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }

        });

        //FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);


        initializeBroadcastListener();
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(CreateLocationSharingActivity.this);
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
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_create_haystack_done, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_done){
            createLocationSharing();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Boolean validate(){
        if(selectedUser == null) return false;
        return true;
    }

    private void createLocationSharing(){
        selectedUser = pagerAdapter.getSelectedUser();

        if(!validate()) {
            Toast.makeText(this, "Please select a user from the list", Toast.LENGTH_SHORT).show();
            return;
        }
        locationSharingVO = new LocationSharingVO();

        //Sender
        locationSharingVO.setSender(Needle.userModel.getUser());

        //Receiver
        locationSharingVO.setReceiver(selectedUser);

        //Options
        String dateLimit = pagerAdapter.getDateLimit();
        String timeLimit = pagerAdapter.getTimeLimit();

        locationSharingVO.setTimeLimit(dateLimit + " " + timeLimit);

        ApiClient.getInstance().createLocationSharing(locationSharingVO, locationSharingCreatedCallback);
    }

    private Callback<LocationSharingResult> locationSharingCreatedCallback = new Callback<LocationSharingResult>() {
        @Override
        public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
            LocationSharingResult result = response.body();

            if(result.getSuccessCode() == 1){
                LocationSharingVO vo = result.getLocationSharing();

                //Add Post Location Request to the DB
                AddPostLocationRequestParams params = new AddPostLocationRequestParams(CreateLocationSharingActivity.this, LocationServiceDBHelper.PostLocationRequest.POSTER_TYPE_LOCATION_SHARING,
                        vo.getTimeLimit(), vo.getSender().getId(), String.valueOf(vo.getId()));
                new AddPostLocationRequestTask(params, null).execute();

                //Feedback
                Toast.makeText(CreateLocationSharingActivity.this, "Location shared with " + result.getLocationSharing().getReceiver().getReadableUserName(), Toast.LENGTH_SHORT).show();

                //Launch Activity
                Intent locationSharingIntent = new Intent(CreateLocationSharingActivity.this, LocationSharingActivity.class);
                locationSharingIntent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Parcelable) result.getLocationSharing());
                startActivity(locationSharingIntent);
            }else{
                Log.e(TAG, "Location Sharing not created. Error : " + result.getMessage());
                Toast.makeText(CreateLocationSharingActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<LocationSharingResult> call, Throwable t) {
            Log.e(TAG, "Location Sharing not created. Error : " + t.getMessage());
            Toast.makeText(CreateLocationSharingActivity.this, "Location Sharing not created !", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();    }
}
