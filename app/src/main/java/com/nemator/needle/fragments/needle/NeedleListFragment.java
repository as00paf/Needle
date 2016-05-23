package com.nemator.needle.fragments.needle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.CreateNeedleActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.NeedlePagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.NeedleResult;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeedleListFragment extends Fragment {
    public static String TAG = "NeedleListFragment";

    //Views
    private View rootView;
    private ViewPager locationSharingViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private FloatingActionButton fab;

    //Objects
    private NeedlePagerAdapter mNeedlePagerAdapter;

    //Data
    public ArrayList<NeedleVO> receivedLocationsList = new ArrayList<>();
    public ArrayList<NeedleVO> sentLocationsList = new ArrayList<>();
    private OnActivityStateChangeListener stateChangeCallback;
    private long lastUpdate;

    public static NeedleListFragment newInstance() {
        NeedleListFragment fragment = new NeedleListFragment();
        return fragment;
    }

    public NeedleListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            NeedleListTabFragment receivedTab = mNeedlePagerAdapter.getReceivedFragment();
            NeedleListTabFragment sentTab = mNeedlePagerAdapter.getSentFragment();

            receivedLocationsList = savedInstanceState.getParcelableArrayList("receivedLocationsList");
            sentLocationsList = savedInstanceState.getParcelableArrayList("sentLocationsList");

            if(receivedLocationsList != null && getActivity() != null){
                ((HomeActivity) getActivity()).setNeedleCount(receivedLocationsList.size());
            }

            if(receivedTab != null) receivedTab.updateNeedlesList(receivedLocationsList);
            if(sentTab != null) sentTab.updateNeedlesList(sentLocationsList);
        }

       // setRetainInstance(true);
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
            rootView = inflater.inflate(R.layout.fragment_needle_list, container, false);

            //FAB
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), CreateNeedleActivity.class);
                    startActivity(intent);
                }
            });

            //View pager
            mNeedlePagerAdapter = new NeedlePagerAdapter(getActivity().getSupportFragmentManager(), this);
            locationSharingViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
            locationSharingViewPager.setOffscreenPageLimit(2);
            locationSharingViewPager.setAdapter(mNeedlePagerAdapter);

            //Tabs
            mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
            mSlidingTabLayout.setDistributeEvenly(true);
            mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

            mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getContext(), android.R.color.white));
            mSlidingTabLayout.setViewPager(locationSharingViewPager);
            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                        case 0:
                            stateChangeCallback.onStateChange(AppState.NEEDLE_RECEIVED_TAB);
                            break;
                        case 1:
                            stateChangeCallback.onStateChange(AppState.NEEDLE_SENT_TAB);
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

    public void fetchLocationSharing(Boolean force){
        long now = new Date().getTime();

        if(!force){
            if(now < lastUpdate + 5000){
                return;
            }
        }

        lastUpdate = now;

        ApiClient.getInstance().fetchNeedles(new Callback<NeedleResult>() {
            @Override
            public void onResponse(Call<NeedleResult> call, Response<NeedleResult> response) {
                NeedleListTabFragment receivedTab = mNeedlePagerAdapter.getReceivedFragment();
                NeedleListTabFragment sentTab = mNeedlePagerAdapter.getSentFragment();

                Log.d(TAG, "location sharing fetched !");

                if(receivedTab != null) receivedTab.setRefreshing(false);
                if(sentTab != null) sentTab.setRefreshing(false);

                NeedleResult result = response.body();
                if(result.getSuccessCode() == 1){
                    receivedLocationsList = result.getReceivedLocationSharings();
                    sentLocationsList = result.getSentLocationSharings();

                    for (NeedleVO vo : receivedLocationsList ) {
                        vo.setReceiver(Needle.userModel.getUser());
                    }

                    for (NeedleVO vo : sentLocationsList ) {
                        vo.setSender(Needle.userModel.getUser());
                    }

                    if(receivedLocationsList != null && getActivity() != null){
                        ((HomeActivity) getActivity()).setNeedleCount(receivedLocationsList.size() + sentLocationsList.size());
                    }

                    if(receivedTab != null) receivedTab.updateNeedlesList(receivedLocationsList);
                    if(sentTab != null) sentTab.updateNeedlesList(sentLocationsList);
                }else{
                    Log.e(TAG, "Could not fetch location sharings. Error : " + result.getMessage());
                    Toast.makeText(getActivity(), R.string.fetch_needles_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NeedleResult> call, Throwable t) {
                NeedleListTabFragment receivedTab = mNeedlePagerAdapter.getReceivedFragment();
                NeedleListTabFragment sentTab = mNeedlePagerAdapter.getSentFragment();

                receivedTab.getRefreshLayout().setRefreshing(false);
                sentTab.getRefreshLayout().setRefreshing(false);

                Log.e(TAG, "Could not fetch location sharings. Error : " + t.getMessage());
                Toast.makeText(getContext(), R.string.fetch_needles_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchLocationSharing(true);
    }

    public void goToPage(int page){
        if(locationSharingViewPager!=null){
            locationSharingViewPager.setCurrentItem(page);
        }
    }
}
