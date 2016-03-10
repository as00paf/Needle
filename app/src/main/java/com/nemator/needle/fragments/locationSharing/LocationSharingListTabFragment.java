package com.nemator.needle.fragments.locationSharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.adapter.LocationSharingListCardAdapter;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;

public class LocationSharingListTabFragment extends Fragment implements LocationSharingCardListener{
    private static final String TAG = "LocationSharingListTabFragment";

    //Views
    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<LocationSharingVO> dataList;
    private Boolean isReceived;

    //Objects
    private RecyclerView.Adapter listAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private OnActivityStateChangeListener stateChangeCallback;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            isReceived = args.getBoolean("isReceived", true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = Needle.navigationController;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener and LocationSharingCardListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_sharing_tab, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.location_sharing_list);
        mRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        listAdapter = new LocationSharingListCardAdapter(dataList, getActivity(), !isReceived, this);
        mRecyclerView.setAdapter(listAdapter);


        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshList();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        int state = isReceived ? AppState.LOCATION_SHARING_RECEIVED_TAB : AppState.LOCATION_SHARING_SENT_TAB;
        if(stateChangeCallback!=null) stateChangeCallback.onStateChange(state);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(listAdapter != null){
            mRecyclerView.setAdapter(listAdapter);
        }
    }

    public void updateLocationSharingList(ArrayList<LocationSharingVO> data){
        this.dataList = data;
        listAdapter = new LocationSharingListCardAdapter(dataList, getActivity(), !isReceived, this);

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(listAdapter);
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }

    //Handlers
    @Override
    public void onRefreshList() {
        Needle.navigationController.refreshLocationSharingList();
    }
}
