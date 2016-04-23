package com.nemator.needle.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
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
import android.view.View;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.CreateHaystackPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackGeneralInfosFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackMapFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackUsersFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.imageUploader.ImageUploadParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadResult;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.CameraUtils;
import com.nemator.needle.views.SlidingTabLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateHaystackActivity extends AppCompatActivity implements ImageUploaderTask.ImageUploadResponseHandler,
        CreateHaystackGeneralInfosFragment.OnPrivacySettingsUpdatedListener{

    public static final String TAG = "CreateHaystackActivity";

    //TODO : put this in constants
    //Activity Results
    public static int TAKE_PICTURE = 226;

    //View
    private FloatingActionButton fab;
    private SlidingTabLayout mSlidingTabLayout;

    //Data
    private String userName;
    private HaystackVO haystack;
    private int userId = -1;
    private ArrayList<UserVO> userList = new ArrayList<UserVO>();

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;
    private Boolean isPublic = false;
    private OnActivityStateChangeListener stateChangeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stateChangeCallback = Needle.navigationController;

        //Toolbar
        setContentView(R.layout.activity_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ViewPager
        mCreateHaystackPagerAdapter = new CreateHaystackPagerAdapter(getSupportFragmentManager(), this);
        createHaystackViewPager = (ViewPager) findViewById(R.id.view_pager);
        createHaystackViewPager.setAdapter(mCreateHaystackPagerAdapter);
        createHaystackViewPager.setOffscreenPageLimit(2);
        createHaystackViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //FAB & Back-Stack
                switch (position) {
                    case 0:
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                        break;
                    case 1:
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location_found));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_MAP);
                        break;
                    case 2:
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_USERS);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Tabs
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.layout_tab_title, R.id.tab_text);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(createHaystackViewPager, toolbar);

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }

        });

        //FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = createHaystackViewPager.getCurrentItem();
                switch (position) {
                    case 0:
                        //Take Picture
                        Intent intent = CameraUtils.getImageCaptureIntent();
                        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                    case 1:

                        //TODO : Add method for that
                        //Focus Camera on current position
                        ((CreateHaystackMapFragment) mCreateHaystackPagerAdapter.getFragmentByType(
                                        CreateHaystackMapFragment.class)).getMapController().focusOnMyPosition();
                        break;
                    case 2:
                        //Invite Friend

                        break;
                }
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && data != null){
            Bundle extras = data.getExtras();

            Bitmap bitmap = (Bitmap) extras.get("data");
            //Rotate if portrait
            if(bitmap.getHeight() > bitmap.getWidth()){
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            CreateHaystackGeneralInfosFragment fragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentAt(0);
            fragment.updatePhoto(bitmap);
        }
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
                //setupSearchView(menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void createHaystack(){
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
            Toast.makeText(this, "Haystack Invalid !", Toast.LENGTH_SHORT);
            return;
        }

        //File Upload
        Bitmap image = mCreateHaystackGenInfosFragment.getPicture();
        if(image != null){
            ImageUploadParams uploadParams = new ImageUploadParams(image, haystack.getName(), this);
            try{
                ImageUploaderTask uploadTask = new ImageUploaderTask(uploadParams, this);
                uploadTask.execute();
            }catch (Exception e) {
                Toast.makeText(this, "An error occured while uploading Haystack's Image", Toast.LENGTH_SHORT).show();
            }
        }else{
            //Create Haystack
            ApiClient.getInstance().createHaystack(haystack, createHaystackHandler);
        }
    }

    private Callback<HaystackResult> createHaystackHandler = new Callback<HaystackResult>() {
        @Override
        public void onResponse(Call<HaystackResult> call, Response<HaystackResult> response) {
            HaystackResult result = response.body();
            if(result.getSuccessCode() == 0){
                Toast.makeText(CreateHaystackActivity.this, "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(CreateHaystackActivity.this, getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

                //TODO : move this part to nav controller
                Intent haystackIntent = new Intent(CreateHaystackActivity.this, HaystackActivity.class);
                haystackIntent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) result.getHaystack());
                CreateHaystackActivity.this.startActivity(haystackIntent);
            }
        }

        @Override
        public void onFailure(Call<HaystackResult> call, Throwable t) {
            Log.d(TAG, "An error occured while creating Haystack : " + t.getMessage());
            Toast.makeText(CreateHaystackActivity.this, "An error occured while creating Haystack ", Toast.LENGTH_SHORT).show();
        }
    };

    //TODO: finish validation
    private Boolean validateHaystack(HaystackVO haystack){
        if(haystack.getName().isEmpty()) return false;

        return true;
    }

    public void onImageUploaded(ImageUploadResult result)
    {
        if(result.successCode == 1) {
            haystack.setPictureURL(result.imageURL);

            //Create Haystack
            ApiClient.getInstance().createHaystack(haystack, createHaystackHandler);
        }
    }

    public HaystackVO getHaystack(){
        return haystack;
    }

    @Override
    public void onPrivacySettingsChanged(Boolean isPublic) {
        mCreateHaystackPagerAdapter.setIsPublic(isPublic);

        this.isPublic = isPublic;
    }

    public void goToPage(int page){
        createHaystackViewPager.setCurrentItem(page);
    }
}
