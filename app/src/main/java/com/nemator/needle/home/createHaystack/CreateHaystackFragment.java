package com.nemator.needle.home.createHaystack;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.nemator.needle.models.Haystack;
import com.nemator.needle.models.User;
import com.nemator.needle.R;
import com.nemator.needle.home.task.createHaystack.CreateHaystackTask;
import com.shamanland.fab.FloatingActionButton;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class CreateHaystackFragment extends Fragment implements CreateHaystackTask.CreateHaystackResponseHandler{
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHaystackFragment";

    public static int TAKE_PICTURE = 1;

    private View rootView;

    private ListView userListView;
    private HaystackUserListAdapter userListAdapter;

    private String userName;
    private Haystack haystack;
    private int userId = -1;
    private ArrayList<User> userList = new ArrayList<User>();

    Boolean mIsMapMoveable = false;
    private GoogleMap mMap;
    public static boolean mMapIsTouched = false;
    Projection projection;
    public double latitude;
    public double longitude;
    ArrayList<LatLng> val = new ArrayList<LatLng>();
    private float mScaleFactor = 1.f;
    private CreateHaystackMap mMapFragment;
    private ScaleGestureDetector mScaleDetector;
    private Boolean mIsCircle = true;

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;
    private Button nextButton, backButton;
    private FloatingActionButton fab;

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
        final CirclePageIndicator viewPagerIndicator = (CirclePageIndicator) rootView.findViewById(R.id.view_pager_indicator);
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
                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                    case 1:
                        //Focus Camera on current position

                        break;
                    case 2:
                        //Done button

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

        mMapFragment = ((CreateHaystackMap) getChildFragmentManager().findFragmentById(R.id.create_haystack_map));

        String title = getResources().getString(R.string.create_haystack);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);

        return rootView;
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

    private void getMap(){
        mMap = mMapFragment.getMap();
    }

    private void addRemoveUsers(){
        Intent intent = new Intent(getActivity(), HaystackUserActivity.class);
        intent.putParcelableArrayListExtra(AppConstants.TAG_ADDED_USERS, userList);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.ADD_REMOVE_USERS);
        startActivityForResult(intent, HaystackUserActivity.ADD_REMOVE_USERS);
    }

    /*private void createHaystack(){
        haystack = new Haystack();
        haystack.setName(txtName.getText().toString());
        haystack.setIsPublic(isPublicCheckbox.isChecked());
        haystack.setTimeLimit(dateLimit + " " + timeLimit);
        haystack.setOwner(getUserId());

        //Current User
        User user = new User();
        user.setUserId(getUserId());
        user.setUserName(getUserName());

        //Users
        userList.add(user);
        haystack.setUsers(userList);

        //Active users
        ArrayList<User> activeUsers = new ArrayList<User>();
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<User> bannedUsers = new ArrayList<User>();
        haystack.setBannedUsers(bannedUsers);

        CreateHaystackTaskParams params = new CreateHaystackTaskParams(rootView.getContext(), haystack);
        try{
            CreateHaystackTask task = new CreateHaystackTask(params, this);
            task.execute();

        }catch (Exception e) {
            Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }
    }*/

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
        if (resultCode == getActivity().RESULT_OK && requestCode == HaystackUserActivity.ADD_REMOVE_USERS) {
            userList = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
            updateUserList();
        }
        else if (requestCode == CreateHaystackFragment.TAKE_PICTURE && resultCode== getActivity().RESULT_OK && data != null){
            Bundle extras = data.getExtras();

            Bitmap bitmap = (Bitmap) extras.get("data");
            CreateHaystackGeneralInfosFragment fragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentAt(0);
            fragment.updatePhoto(bitmap);
        }
    }

    private void updateUserList(){
        userListAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, userList, getLayoutInflater(null));
        userListAdapter.notifyDataSetChanged();

        userListView.setAdapter(userListAdapter);
        userListView.invalidate();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public Haystack getHaystack(){
        return haystack;
    }
}
