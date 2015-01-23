package com.needletest.pafoid.needletest.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.User;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;


public class HaystackMapFragment extends Fragment {
    public static final String TAG = "HaystackMapFragment";

    private int userId = -1;
    private Haystack haystack;
    private boolean isOwner;

    private View rootView;
    public CustomSupportMapFragment mMapFragment;

    public static HaystackMapFragment newInstance() {
        HaystackMapFragment fragment = new HaystackMapFragment();
        return fragment;
    }

    public HaystackMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        haystack = ((HaystackActivity) getActivity()).getHaystack();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_haystack_map, container, false);

        mMapFragment = CustomSupportMapFragment.newInstance();

        getChildFragmentManager().beginTransaction().add(R.id.haystack_map_container, mMapFragment).commit();
        mMapFragment.setRetainInstance(true);

        isOwner = haystack.getOwner() == getUserId();
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_users);
        if(isOwner){
            fab.setSize(FloatingActionButton.SIZE_NORMAL);
            fab.setColor(getResources().getColor(R.color.primary));

            fab.initBackground();
            fab.setImageResource(R.drawable.ic_action_add_person);
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addUsers();
                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    private void addUsers(){
        Intent intent = new Intent(getActivity(), HaystackUserActivity.class);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.ADD_USERS);
        intent.putExtra(AppConstants.TAG_HAYSTACK_ID, haystack.getId());
        startActivityForResult(intent, HaystackUserActivity.ADD_USERS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == HaystackUserActivity.ADD_USERS) {
            ArrayList<User> addedUsers = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
            ((HaystackActivity) getActivity()).getHaystack().getUsers().addAll(addedUsers);
            haystack = ((HaystackActivity) getActivity()).getHaystack();
        }
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }
}
