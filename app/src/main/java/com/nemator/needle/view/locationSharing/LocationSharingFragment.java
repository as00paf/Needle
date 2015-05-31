package com.nemator.needle.view.locationSharing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksParams;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksResult;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksTask;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingParams;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingResult;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingTask;
import com.nemator.needle.view.home.HaystackListCardAdapter;

import java.util.ArrayList;


public class LocationSharingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FetchLocationSharingTask.FetchLocationSharingResponseHandler {
    public static String TAG = "LocationSharingFragment";

    //Views
    private View rootView;
    private ProgressBar progressbar = null;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Objects
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Data
    private ArrayList<LocationSharingVO> dataList;
    private int userId = -1;

    public static LocationSharingFragment newInstance() {
        LocationSharingFragment fragment = new LocationSharingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LocationSharingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_sharing, container, false);

        //Progress bar
        progressbar = (ProgressBar) rootView.findViewById(R.id.location_sharing_progress_bar);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.location_sharing_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new LocationSharingListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchLocationSharing();
    }

    @Override
    public void onRefresh() {
        fetchLocationSharing();
    }

    public void fetchLocationSharing(){
        FetchLocationSharingParams params = new FetchLocationSharingParams(String.valueOf(getUserId()), rootView.getContext(), progressbar);

        try{
            FetchLocationSharingTask task = new FetchLocationSharingTask(params, this);
            task.execute();
        }catch(Exception e){
            Log.e(TAG, "fetchLocationSharing exception : " + e.toString());
        }
    }

    public void onHaystackFetched(FetchLocationSharingResult result){
        dataList = result.locationSharingList;
        updateLocationSharingList();
    }

    public void updateLocationSharingList() {
        if(rootView == null){
            return;
        }

        mAdapter = new LocationSharingListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        progressbar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onLocationSharingFetched(FetchLocationSharingResult result) {

    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }
}
