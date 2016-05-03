package com.nemator.needle.fragments.haystacks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.HaystackPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.controller.NavigationController;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackListFragment extends Fragment implements HaystackListTabFragment.HaystackListFragmentInteractionListener {
    private static final String TAG = "HaystackListFragment";

    //Views
    private View rootView;
    private FloatingActionButton fab;
    private ViewPager haystackListViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    //Objects
    private HaystackPagerAdapter mHaystackPagerAdapter;
    private OnActivityStateChangeListener stateChangeCallback;

    //Data
    public ArrayList<HaystackVO> publicHaystacks = null;
    public ArrayList<HaystackVO> privateHaystacks = null;
    public ArrayList<HaystackVO> ownedHaystacks = null;
    private long lastUpdate;

    public static HaystackListFragment newInstance() {
        HaystackListFragment fragment = new HaystackListFragment();
        return fragment;
    }

    public HaystackListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            publicHaystacks = savedInstanceState.getParcelableArrayList("publicHaystacks");
            privateHaystacks = savedInstanceState.getParcelableArrayList("privateHaystacks");
            ownedHaystacks = savedInstanceState.getParcelableArrayList("ownedHaystacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TODO : use constants
        outState.putParcelableArrayList("publicHaystacks", publicHaystacks);
        outState.putParcelableArrayList("privateHaystacks", privateHaystacks);
        outState.putParcelableArrayList("ownedHaystacks", ownedHaystacks);

        super.onSaveInstanceState(outState);
    }

    //TODO : remove this
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = ((OnActivityStateChangeListener) Needle.navigationController);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_haystack_list, container, false);

        //Navigation Drawer
        Boolean firstNavDrawerLearned = getContext().getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE).getBoolean("firstNavDrawerLearned", false);

        if(!firstNavDrawerLearned){
            SharedPreferences.Editor edit = getContext().getSharedPreferences("com.nemator.needle", Context.MODE_PRIVATE).edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }

        //FAB
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationController.getInstance().showSection(AppConstants.SECTION_CREATE_HAYSTACK);
            }
        });

        //View pager
        haystackListViewPager = (ViewPager) rootView.findViewById(R.id.haystackListViewPager);
        mHaystackPagerAdapter = new HaystackPagerAdapter(getActivity().getSupportFragmentManager(), getContext(), this);
        haystackListViewPager.setOffscreenPageLimit(3);
        haystackListViewPager.setAdapter(mHaystackPagerAdapter);

        //Tabs
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.haystack_list_sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
       /* mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
*/
        mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getContext(), android.R.color.white));
        mSlidingTabLayout.setViewPager(haystackListViewPager);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                        stateChangeCallback.onStateChange(AppState.PUBLIC_HAYSTACK_TAB);
                        break;
                    case 1:
                        stateChangeCallback.onStateChange(AppState.PRIVATE_HAYSTACK_TAB);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return rootView;
    }

    public void fetchHaystacks(Boolean force){
        long now = new Date().getTime();

        if(!force){
            if(now < lastUpdate + 5000){
                return;
            }
        }

        lastUpdate = now;
        ApiClient.getInstance().fetchHaystacks(getUserId(), new Callback<HaystackResult>() {

            @Override
            public void onResponse(Call<HaystackResult> call, Response<HaystackResult> response) {
                HaystackResult result = response.body();

                Log.d(TAG, "haystacks fetched !");

                HaystackListTabFragment publicTab = mHaystackPagerAdapter.getPublicHaystackListFragment();
                HaystackListTabFragment privateTab = mHaystackPagerAdapter.getPrivateHaystackListFragment();
                HaystackListTabFragment ownedTab = mHaystackPagerAdapter.getOwnedHaystackListFragment();

                if (publicTab != null) publicTab.getRefreshLayout().setRefreshing(false);
                if (privateTab != null) privateTab.getRefreshLayout().setRefreshing(false);
                if (ownedTab != null) ownedTab.getRefreshLayout().setRefreshing(false);

                if (result.getSuccessCode() == 1) {
                    publicHaystacks = result.getPublicHaystacks();
                    privateHaystacks = result.getPrivateHaystacks();
                    ownedHaystacks = result.getOwnedHaystacks();

                    if (publicTab != null) publicTab.updateHaystackList(publicHaystacks);
                    if (privateTab != null) privateTab.updateHaystackList(privateHaystacks);
                    if (ownedTab != null) ownedTab.updateHaystackList(ownedHaystacks);

                    if (privateHaystacks != null && publicHaystacks != null && getActivity() != null) {
                        ((HomeActivity) getActivity()).setHaystacksCount(privateHaystacks.size() + publicHaystacks.size());
                    }

                } else if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.fetch_haystack_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HaystackResult> call, Throwable t) {
                Log.d(TAG, "haystacks fetch failed !");

                HaystackListTabFragment publicTab = mHaystackPagerAdapter.getPublicHaystackListFragment();
                HaystackListTabFragment privateTab = mHaystackPagerAdapter.getPrivateHaystackListFragment();
                HaystackListTabFragment ownedTab = mHaystackPagerAdapter.getOwnedHaystackListFragment();

                publicTab.getRefreshLayout().setRefreshing(false);
                privateTab.getRefreshLayout().setRefreshing(false);
                ownedTab.getRefreshLayout().setRefreshing(false);

                Toast.makeText(getContext(), R.string.fetch_haystack_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchHaystacks(true);
    }

    private int getUserId(){
        return Needle.userModel.getUserId();
    }

    public void goToTab(int tab){
        haystackListViewPager.setCurrentItem(tab);
    }

    @Override
    public void onRefreshHaystackList() {
        fetchHaystacks(true);
    }

    @Override
    public void onClickHaystackCard(HaystackVO haystack) {
        Intent haystackIntent = new Intent(getActivity(), HaystackActivity.class);
        haystackIntent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) haystack);
        startActivity(haystackIntent);
    }

    @Override
    public void onCancelHaystack(HaystackVO haystack) {
        ApiClient.getInstance().cancelHaystack(Needle.userModel.getUser(), haystack, haystackCancelledCallback);
    }

    private Callback<TaskResult> haystackCancelledCallback = new Callback<TaskResult>() {
        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.i(TAG, "Haystack Cancelled !");
                Toast.makeText(getContext(), "Haystack Cancelled", Toast.LENGTH_SHORT).show();
                onRefreshHaystackList();
            }else{
                Log.i(TAG, "Could not cancel Haystack. Error : " + result.getMessage());
                Toast.makeText(getContext(), "Could not cancel Haystack", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {
            Log.i(TAG, "Could not cancel Haystack. Error : " + t.getMessage());
        }
    };

    @Override
    public void onLeaveHaystack(HaystackVO haystack) {
        ApiClient.getInstance().leaveHaystack(Needle.userModel.getUser(), haystack, haystackLeftCallback);
    }

    private Callback<TaskResult> haystackLeftCallback = new Callback<TaskResult>() {
        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.i(TAG, "Haystack Left !");
                Toast.makeText(getContext(), "Haystack Left", Toast.LENGTH_SHORT).show();
                onRefreshHaystackList();
            }else{
                Log.i(TAG, "Could not leave Haystack. Error : " + result.getMessage());
                Toast.makeText(getContext(), "Could not leave Haystack", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {

        }
    };
}
