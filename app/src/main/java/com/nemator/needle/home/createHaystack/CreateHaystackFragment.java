package com.nemator.needle.home.createHaystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
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

import java.util.ArrayList;

public class CreateHaystackFragment extends Fragment implements CreateHaystackTask.CreateHaystackResponseHandler{
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHaystackFragment";

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
        createHaystackViewPager = (ViewPager) rootView.findViewById(R.id.haystackListViewPager);
        createHaystackViewPager.setAdapter(mCreateHaystackPagerAdapter);

        //Bottom Sheet Buttons
        nextButton = (Button) rootView.findViewById(R.id.create_haystack_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() + 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                backButton.setEnabled(true);
                nextButton.setEnabled(newIndex != 2);
            }
        });

        backButton = (Button) rootView.findViewById(R.id.create_haystack_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() - 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                backButton.setEnabled(newIndex != 0);
            }
        });

        mMapFragment = ((CreateHaystackMap) getChildFragmentManager().findFragmentById(R.id.create_haystack_map));

        String title = getResources().getString(R.string.create_haystack);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);

        return rootView;
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
