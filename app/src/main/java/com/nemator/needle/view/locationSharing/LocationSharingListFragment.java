package com.nemator.needle.view.locationSharing;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingParams;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingResult;
import com.nemator.needle.tasks.fetchLocationSharing.FetchLocationSharingTask;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;


public class LocationSharingListFragment extends Fragment implements FetchLocationSharingTask.FetchLocationSharingResponseHandler {
    public static String TAG = "LocationSharingFragment";

    //Views
    private View rootView;
    private ViewPager locationSharingViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private FloatingActionButton fab;

    //Objects
    private LocationSharingPagerAdapter mLocationSharingPagerAdapter;
    private LocationSharingListFragmentInteractionListener fabListener;

    //Data
    public ArrayList<LocationSharingVO> receivedLocationsList = new ArrayList<>();
    public ArrayList<LocationSharingVO> sentLocationsList = new ArrayList<>();
    private OnActivityStateChangeListener stateChangeCallback;

    public static LocationSharingListFragment newInstance() {
        LocationSharingListFragment fragment = new LocationSharingListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LocationSharingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            receivedLocationsList = savedInstanceState.getParcelableArrayList("receivedLocationsList");
            sentLocationsList = savedInstanceState.getParcelableArrayList("sentLocationsList");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("receivedLocationsList", receivedLocationsList);
        outState.putParcelableArrayList("sentLocationsList", sentLocationsList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = (OnActivityStateChangeListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_location_sharing, container, false);

            //FAB
            fabListener = ((MainActivity) getActivity());
            fab = (FloatingActionButton) rootView.findViewById(R.id.location_sharing_fab);
            fab.setColor(getResources().getColor(R.color.primary));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fabListener.onCreateLocationSharingFabTapped();
                }
            });
            fab.initBackground();

            //View pager
            mLocationSharingPagerAdapter = new LocationSharingPagerAdapter(getActivity().getSupportFragmentManager(), this);
            locationSharingViewPager = (ViewPager) rootView.findViewById(R.id.location_sharing_view_pager);
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
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchLocationSharing();
    }

    public void fetchLocationSharing(){
       FetchLocationSharingParams params = new FetchLocationSharingParams(String.valueOf(getUserId()), rootView.getContext());

        try{
            FetchLocationSharingTask task = new FetchLocationSharingTask(params, this);
            task.execute();
        }catch(Exception e){
            Log.e(TAG, "fetchLocationSharing exception : " + e.toString());
        }
    }

    @Override
    public void onLocationSharingFetched(FetchLocationSharingResult result) {
        receivedLocationsList = result.receivedLocationSharingList;
        sentLocationsList = result.sentLocationSharingList;

        if(receivedLocationsList != null && getActivity() != null){
            ((MainActivity) getActivity()).setLocationSharingCount(receivedLocationsList.size());
        }

        LocationSharingListTabFragment receivedTab = mLocationSharingPagerAdapter.getReceivedFragment();
        LocationSharingListTabFragment sentTab = mLocationSharingPagerAdapter.getSentFragment();

        receivedTab.getRefreshLayout().setRefreshing(false);
        sentTab.getRefreshLayout().setRefreshing(false);

        receivedTab.updateLocationSharingList(receivedLocationsList);
        sentTab.updateLocationSharingList(sentLocationsList);
    }

    private int getUserId() {
        return ((MainActivity) getActivity()).getUserId();
    }

    public void goToPage(int page){
        locationSharingViewPager.setCurrentItem(page);
    }

    //Interface
    public interface LocationSharingListFragmentInteractionListener {
        void onCreateLocationSharingFabTapped();
        void onRefreshLocationSharingList();
        void onClickLocationSharingCard(LocationSharingVO locationSharing, Boolean isSent);
    }
}
