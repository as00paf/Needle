package com.nemator.needle.fragments.locationSharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.CreateLocationSharingActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.LocationSharingPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationSharingListFragment extends Fragment {
    public static String TAG = "LocationSharingFragment";

    //Views
    private View rootView;
    private ViewPager locationSharingViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private FloatingActionButton fab;

    //Objects
    private LocationSharingPagerAdapter mLocationSharingPagerAdapter;

    //Data
    public ArrayList<LocationSharingVO> receivedLocationsList = new ArrayList<>();
    public ArrayList<LocationSharingVO> sentLocationsList = new ArrayList<>();
    private OnActivityStateChangeListener stateChangeCallback;
    private long lastUpdate;

    public static LocationSharingListFragment newInstance() {
        LocationSharingListFragment fragment = new LocationSharingListFragment();
        return fragment;
    }

    public LocationSharingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            LocationSharingListTabFragment receivedTab = mLocationSharingPagerAdapter.getReceivedFragment();
            LocationSharingListTabFragment sentTab = mLocationSharingPagerAdapter.getSentFragment();

            receivedLocationsList = savedInstanceState.getParcelableArrayList("receivedLocationsList");
            sentLocationsList = savedInstanceState.getParcelableArrayList("sentLocationsList");

            if(receivedLocationsList != null && getActivity() != null){
                ((HomeActivity) getActivity()).setLocationSharingCount(receivedLocationsList.size());
            }

            if(receivedTab != null) receivedTab.updateLocationSharingList(receivedLocationsList);
            if(sentTab != null) sentTab.updateLocationSharingList(sentLocationsList);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TODO : use constants
        outState.putParcelableArrayList("receivedLocationsList", receivedLocationsList);
        outState.putParcelableArrayList("sentLocationsList", sentLocationsList);
        super.onSaveInstanceState(outState);
    }

    //TODO : remove this
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = Needle.navigationController;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_location_sharing_list, container, false);

            //FAB
            fab = (FloatingActionButton) rootView.findViewById(R.id.location_sharing_fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), CreateLocationSharingActivity.class);
                    startActivity(intent);
                }
            });

            //View pager
            mLocationSharingPagerAdapter = new LocationSharingPagerAdapter(getActivity().getSupportFragmentManager(), this);
            locationSharingViewPager = (ViewPager) rootView.findViewById(R.id.location_sharing_view_pager);
            locationSharingViewPager.setOffscreenPageLimit(2);
            locationSharingViewPager.setAdapter(mLocationSharingPagerAdapter);

            //Tabs
            mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.location_sharing_sliding_tabs);
            mSlidingTabLayout.setDistributeEvenly(true);
            mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

            mSlidingTabLayout.setViewPager(locationSharingViewPager);
            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                        case 0:
                            stateChangeCallback.onStateChange(AppState.LOCATION_SHARING_RECEIVED_TAB);
                            break;
                        case 1:
                            stateChangeCallback.onStateChange(AppState.LOCATION_SHARING_SENT_TAB);
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            setRetainInstance(true);
        }

        return rootView;
    }

    public void fetchLocationSharing(Boolean force){
        long now = new Date().getTime();

        if(!force){
            if(now < lastUpdate + 5000){
                return;
            }
        }

        lastUpdate = now;

        ApiClient.getInstance().fetchLocationSharings(new Callback<LocationSharingResult>() {
            @Override
            public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
                LocationSharingListTabFragment receivedTab = mLocationSharingPagerAdapter.getReceivedFragment();
                LocationSharingListTabFragment sentTab = mLocationSharingPagerAdapter.getSentFragment();

                Log.d(TAG, "location sharing fetched !");

                if(receivedTab != null) receivedTab.setRefreshing(false);
                if(sentTab != null) sentTab.setRefreshing(false);

                LocationSharingResult result = response.body();
                if(result.getSuccessCode() == 1){
                    receivedLocationsList = result.getReceivedLocationSharings();
                    sentLocationsList = result.getSentLocationSharings();

                    if(receivedLocationsList != null && getActivity() != null){
                        ((HomeActivity) getActivity()).setLocationSharingCount(receivedLocationsList.size());
                    }

                    if(receivedTab != null) receivedTab.updateLocationSharingList(receivedLocationsList);
                    if(sentTab != null) sentTab.updateLocationSharingList(sentLocationsList);
                }else{
                    Log.e(TAG, "Could not fetch location sharings. Error : " + result.getMessage());
                    Toast.makeText(getActivity(), R.string.fetch_location_sharings_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LocationSharingResult> call, Throwable t) {
                LocationSharingListTabFragment receivedTab = mLocationSharingPagerAdapter.getReceivedFragment();
                LocationSharingListTabFragment sentTab = mLocationSharingPagerAdapter.getSentFragment();

                receivedTab.getRefreshLayout().setRefreshing(false);
                sentTab.getRefreshLayout().setRefreshing(false);

                Log.e(TAG, "Could not fetch location sharings. Error : " + t.getMessage());
                Toast.makeText(getActivity(), R.string.fetch_location_sharings_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchLocationSharing(true);
    }

    private int getUserId() {
        return Needle.userModel.getUserId();
    }

    public void goToPage(int page){
        if(locationSharingViewPager!=null){
            locationSharingViewPager.setCurrentItem(page);
        }
    }
}
