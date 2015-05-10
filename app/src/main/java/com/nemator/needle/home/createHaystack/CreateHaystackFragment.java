package com.nemator.needle.home.createHaystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.AppConstants;
import com.nemator.needle.haystack.HaystackActivity;
import com.nemator.needle.haystack.HaystackUserActivity;
import com.nemator.needle.haystack.HaystackUserListAdapter;
import com.nemator.needle.home.task.createHaystack.CreateHaystackResult;
import com.nemator.needle.home.task.createHaystack.CreateHaystackTaskParams;
import com.nemator.needle.home.task.imageUploader.ImageUploadParams;
import com.nemator.needle.home.task.imageUploader.ImageUploadResult;
import com.nemator.needle.home.task.imageUploader.ImageUploaderTask;
import com.nemator.needle.models.Haystack;
import com.nemator.needle.models.User;
import com.nemator.needle.R;
import com.nemator.needle.home.task.createHaystack.CreateHaystackTask;
import com.shamanland.fab.FloatingActionButton;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class CreateHaystackFragment extends Fragment implements CreateHaystackTask.CreateHaystackResponseHandler, ImageUploaderTask.ImageUploadResponseHandler{
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHaystackFragment";

    //Activity Results
    public static int TAKE_PICTURE = 1;

    //View
    private View rootView;
    private Button nextButton, backButton;
    private FloatingActionButton fab;
    private CirclePageIndicator viewPagerIndicator;

    private CreateHaystackGeneralInfosFragment mCreateHaystackGenInfosFragment;
    private CreateHaystackMapFragment mCreateHaystackMapFragment;
    private CreateHaystackUsersFragment mCreateHaystackUsersFragment;

    private CreateHaystackMap mMap;

    //Data
    private HaystackUserListAdapter userListAdapter;
    private String userName;
    private Haystack haystack;
    private int userId = -1;
    private ArrayList<User> userList = new ArrayList<User>();

    Boolean mIsMapMoveable = false;
    public static boolean mMapIsTouched = false;
    Projection projection;
    public double latitude;
    public double longitude;
    ArrayList<LatLng> val = new ArrayList<LatLng>();
    private float mScaleFactor = 1.f;

    private ScaleGestureDetector mScaleDetector;
    private Boolean mIsCircle = true;

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;


    public static CreateHaystackFragment newInstance() {
        CreateHaystackFragment fragment = new CreateHaystackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
        }
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
                nextButton.setEnabled((position == 0 || position == 1));

                //ActionBar

                //FAB
                switch (position){
                    case 0:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_photo_camera_black_24dp));
                        break;
                    case 1:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_action_location_found));
                        break;
                    case 2:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_action_add_person));
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
                        mMap.focusCamera();
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
        nextButton.setEnabled(currentItemIndex != 2);
    }

    private void createHaystack(){
        haystack = new Haystack();
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
        User user = new User();
        user.setUserId(getUserId());
        user.setUserName(getUserName());

        //Users
        userList.add(user);
        userList.addAll(mCreateHaystackUsersFragment.getSelectedUsers());
        haystack.setUsers(userList);

        //Active users
        ArrayList<User> activeUsers = new ArrayList<User>();
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<User> bannedUsers = new ArrayList<User>();
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
                CreateHaystackTask task = new CreateHaystackTask(params, this);
                task.execute();

            }catch (Exception e) {
                Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //TODO: finish validation
    private Boolean validateHaystack(Haystack haystack){
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
                CreateHaystackTask task = new CreateHaystackTask(params, this);
                task.execute();

            }catch (Exception e) {
                Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onHaystackCreated(CreateHaystackResult result){
        if(result.successCode == 0){
            Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }else{
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(this);
            trans.commit();

            Toast.makeText(getActivity(), getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), HaystackActivity.class);
            intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystack);
            startActivity(intent);
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
            CreateHaystackGeneralInfosFragment fragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentAt(0);
            fragment.updatePhoto(bitmap);
        }
    }

    public Haystack getHaystack(){
        return haystack;
    }
}
