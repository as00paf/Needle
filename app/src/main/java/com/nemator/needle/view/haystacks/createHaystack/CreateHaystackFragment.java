package com.nemator.needle.view.haystacks.createHaystack;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.createHaystack.CreateHaystackTask;
import com.nemator.needle.tasks.createHaystack.CreateHaystackTaskParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadResult;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.shamanland.fab.FloatingActionButton;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class CreateHaystackFragment extends CreateHaystackBaseFragment implements ImageUploaderTask.ImageUploadResponseHandler,
        CreateHaystackGeneralInfosFragment.OnPrivacySettingsUpdatedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public static final String TAG = "CreateHaystackFragment";

    //Activity Results
    public static int TAKE_PICTURE = 1;

    //View
    private View rootView;
    private Button nextButton, backButton;
    private FloatingActionButton fab;
    private CirclePageIndicator viewPagerIndicator;

    //Fragments
    public CreateHaystackGeneralInfosFragment mCreateHaystackGenInfosFragment;
    public CreateHaystackMapFragment mCreateHaystackMapFragment;
    public CreateHaystackUsersFragment mCreateHaystackUsersFragment;

    private CreateHaystackMap mMap;

    //Data
    private String userName;
    private HaystackVO haystack;
    private int userId = -1;
    private ArrayList<UserVO> userList = new ArrayList<UserVO>();

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;
    private Boolean isPublic = false;
    private OnActivityStateChangeListener stateChangeCallback;

    public static GoogleApiClient mGoogleApiClient;

    public static CreateHaystackFragment newInstance() {
        CreateHaystackFragment fragment = new CreateHaystackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = ((OnActivityStateChangeListener) ((MainActivity) getActivity()).getNavigationController());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
        }

        //Google API Client
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Toast.makeText(getActivity(), "Google Play Services Unavailable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Play Services Unavailable");
        }else{
            connectToApiClient();
        }
    }

    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }else if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack, container, false);

        //ViewPager
        mCreateHaystackPagerAdapter = new CreateHaystackPagerAdapter(getActivity().getSupportFragmentManager(), this);
        createHaystackViewPager = (ViewPager) rootView.findViewById(R.id.create_haystack_view_pager);
        createHaystackViewPager.setAdapter(mCreateHaystackPagerAdapter);

        //ViewPagerIndicator
        viewPagerIndicator  = (CirclePageIndicator) rootView.findViewById(R.id.view_pager_indicator);
        viewPagerIndicator.setViewPager(createHaystackViewPager);
        viewPagerIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //Back/Next Button
                backButton.setEnabled((position == 1 || position == 2));
                nextButton.setEnabled((position == 0 || (position == 1 && isPublic == false)));

                //FAB & Back-Stack
                switch (position){
                    case 0:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_photo_camera_black_24dp));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                        break;
                    case 1:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_action_location_found));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_MAP);
                        break;
                    case 2:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_action_add_person));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_USERS);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //FAB
        fab = (FloatingActionButton) rootView.findViewById(R.id.new_haystack_photo_fab);
        fab.setColor(getResources().getColor(R.color.primary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = createHaystackViewPager.getCurrentItem();
                switch(position){
                    case 0:
                        //Take Picture
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                    case 1:
                        //Focus Camera on current position
                        mMap = ((CreateHaystackMapFragment) getFragmentManager().getFragments().get(4)).getMap();
                        mMap.moveUserToCurrentPosition();
                        break;
                    case 2:
                        //Invite Friend

                        break;
                }

            }
        });
        fab.initBackground();

        //Bottom Sheet Buttons
        nextButton = (Button) rootView.findViewById(R.id.create_haystack_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() + 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                updateButtonsState();
            }
        });

        backButton = (Button) rootView.findViewById(R.id.create_haystack_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() - 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                updateButtonsState();
            }
        });

        String title = getResources().getString(R.string.create_haystack);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int position = createHaystackViewPager.getCurrentItem();
        switch(position){
            case 0:
                super.onCreateOptionsMenu(menu, inflater);
                break;
            case 1:
                if(isPublic){
                    inflater.inflate(R.menu.menu_create_haystack_done, menu);
                }else{
                    super.onCreateOptionsMenu(menu, inflater);
                }
                break;
            case 2:
                inflater.inflate(R.menu.menu_create_haystack_done, menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_haystack_action_done) {
            createHaystack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // method to check if you have a Camera
    private boolean hasCamera(){
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // method to check you have Camera Apps
    private boolean hasDefaultCameraApp(String action){
        final PackageManager packageManager = getActivity().getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;
    }

    private void updateButtonsState(){
        int currentItemIndex = createHaystackViewPager.getCurrentItem();
        backButton.setEnabled(currentItemIndex != 0);
        nextButton.setEnabled((currentItemIndex == 0 || (currentItemIndex == 1 && isPublic == false)));
    }

    private void createHaystack(){
        haystack = new HaystackVO();
        mCreateHaystackGenInfosFragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackGeneralInfosFragment.class);
        mCreateHaystackMapFragment = (CreateHaystackMapFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackMapFragment.class);
        mCreateHaystackUsersFragment = (CreateHaystackUsersFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackUsersFragment.class);

        //General Infos
        haystack.setName(mCreateHaystackGenInfosFragment.getHaystackName());
        haystack.setOwner(getUserId());
        haystack.setIsPublic(mCreateHaystackGenInfosFragment.getIsPublic());
        haystack.setTimeLimit(mCreateHaystackGenInfosFragment.getDateLimit() + " " + mCreateHaystackGenInfosFragment.getTimeLimit());

        //Location
        haystack.setZoneRadius(mCreateHaystackMapFragment.getZoneRadius());
        haystack.setPosition(mCreateHaystackMapFragment.getPosition());
        haystack.setIsCircle(mCreateHaystackMapFragment.getIsCircle());

        //Current User
        UserVO user = new UserVO();
        user.setUserId(getUserId());
        user.setUserName(getUserName());

        //Users
        userList.add(user);
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
            Toast.makeText(getActivity(), "Haystack Invalid !", Toast.LENGTH_SHORT);
            return;
        }

        //File Upload
        Bitmap image = mCreateHaystackGenInfosFragment.getPicture();
        if(image != null){
            ImageUploadParams uploadParams = new ImageUploadParams(image, haystack.getName(), rootView.getContext());
            try{
                ImageUploaderTask uploadTask = new ImageUploaderTask(uploadParams, this);
                uploadTask.execute();
            }catch (Exception e) {
                Toast.makeText(getActivity(), "An error occured while uploading Haystack's Image", Toast.LENGTH_SHORT).show();
            }
        }else{
            //Create Haystack
            CreateHaystackTaskParams params = new CreateHaystackTaskParams(rootView.getContext(), haystack);
            try{
                CreateHaystackTask task = new CreateHaystackTask(params, ((MainActivity) getActivity()).getNavigationController());
                task.execute();

            }catch (Exception e) {
                Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //TODO: finish validation
    private Boolean validateHaystack(HaystackVO haystack){
        if(haystack.getName().isEmpty()) return false;

        return true;
    }

    public void onImageUploaded(ImageUploadResult result)
    {
        if(result.successCode == 1){
            haystack.setPictureURL(result.imageURL);
            //Create Haystack
            CreateHaystackTaskParams params = new CreateHaystackTaskParams(rootView.getContext(), haystack);
            try{
                CreateHaystackTask task = new CreateHaystackTask(params, (((MainActivity) getActivity()).getNavigationController()));
                task.execute();

            }catch (Exception e) {
                Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getUserName(){
        if(userName == null){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userName = sp.getString("username", null);
        }

        return userName;
    }

    private int getUserId(){
        if(userId == -1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CreateHaystackFragment.TAKE_PICTURE && resultCode== getActivity().RESULT_OK && data != null){
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

    public HaystackVO getHaystack(){
        return haystack;
    }

    @Override
    public void onPrivacySettingsChanged(Boolean isPublic) {
        mCreateHaystackPagerAdapter.setIsPublic(isPublic);
        viewPagerIndicator.notifyDataSetChanged();

        this.isPublic = isPublic;
    }

    public void goToPage(int page){
        createHaystackViewPager.setCurrentItem(page);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
        mCreateHaystackMapFragment = (CreateHaystackMapFragment) mCreateHaystackPagerAdapter.getFragmentByType(CreateHaystackMapFragment.class);
        mCreateHaystackMapFragment.mMapFragment.onConnected(connectionHint);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), AppConstants.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode){
        Toast.makeText(getActivity(), "Error encountered\nError # :"+errorCode, Toast.LENGTH_SHORT).show();
    }

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
