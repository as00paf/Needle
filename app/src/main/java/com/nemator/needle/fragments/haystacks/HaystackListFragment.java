package com.nemator.needle.fragments.haystacks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.nemator.needle.adapter.HaystackListPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.controller.NavigationController;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;
import java.util.Date;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class HaystackListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "HaystackListFragment";

    //Views
    private View rootView;
    private FloatingActionButton fab;
    private ViewPager haystackListViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private SwipeRefreshLayout refreshLayout;

    //Objects
    private HaystackListPagerAdapter mHaystackListPagerAdapter;
    private OnActivityStateChangeListener stateChangeCallback;
    private HaystackListFragmentInteractionListener fragmentListener;

    //Data
    public ArrayList<HaystackVO> publicHaystacks = null;
    public ArrayList<HaystackVO> privateHaystacks = null;
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("publicHaystacks", publicHaystacks);
        outState.putParcelableArrayList("privateHaystacks", privateHaystacks);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = ((OnActivityStateChangeListener) Needle.navigationController);
            fragmentListener = ((HaystackListFragmentInteractionListener) Needle.navigationController);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_haystack_list, container, false);

            //Navigation Drawer
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Boolean firstNavDrawerLearned = sharedPreferences.getBoolean("firstNavDrawerLearned", false);

            if(!firstNavDrawerLearned){
                //TODO: Open Nav Drawer

                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("firstNavDrawerLearned", true);
                edit.commit();
            }

            //FAB
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavigationController.getInstance().showSection(AppConstants.SECTION_CREATE_HAYSTACK, getActivity());
                }
            });

            //View pager
            mHaystackListPagerAdapter = new HaystackListPagerAdapter(getActivity().getSupportFragmentManager(), this);
            haystackListViewPager = (ViewPager) rootView.findViewById(R.id.haystackListViewPager);
            haystackListViewPager.setAdapter(mHaystackListPagerAdapter);

            //Tabs
            mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.haystack_list_sliding_tabs);
            mSlidingTabLayout.setDistributeEvenly(true);
            mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

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
        }

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
        ApiClient.getInstance().fetchHaystacks(getUserId(), new Callback<HaystackTaskResult>() {

            @Override
            public void onResponse(Response<HaystackTaskResult> response, Retrofit retrofit) {
                HaystackTaskResult result = response.body();

                Log.d(TAG, "haystacks fetched !");

                HaystackListTabFragment publicTab = mHaystackListPagerAdapter.getPublicHaystackListFragment();
                HaystackListTabFragment privateTab = mHaystackListPagerAdapter.getPrivateHaystackListFragment();
/*
            //Show how many haystacks are available in the Nav Drawer
            int count = publicHaystacks.size() + privateHaystacks.size();
            if(getActivity() != null)
                ((HomeActivity) getActivity()).getNavigationController().setHaystacksCount(count);
*/
                if(publicTab == null && privateTab == null) return;

                publicTab.getRefreshLayout().setRefreshing(false);
                privateTab.getRefreshLayout().setRefreshing(false);

                if(result.getSuccessCode() == 1){
                    publicHaystacks = result.getPublicHaystackList();
                    privateHaystacks = result.getPrivateHaystackList();

                    publicTab.updateHaystackList(publicHaystacks);
                    privateTab.updateHaystackList(privateHaystacks);
                }else if(getActivity() != null){
                    Toast.makeText(getActivity(), R.string.fetch_haystack_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "haystacks fetch failed !");
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchHaystacks(false);

        Needle.navigationController.setAccount();
    }

    @Override public void onRefresh() {
        fetchHaystacks(true);
    }

    private int getUserId(){
        return Needle.userModel.getUserId();
    }

    private String getUserName(){
        return Needle.userModel.getUserName();
    }

    public void goToTab(int tab){
        haystackListViewPager.setCurrentItem(tab);
    }

    public interface HaystackListFragmentInteractionListener {
        void onRefreshHaystackList();
        void onClickHaystackCard(HaystackVO haystack);
    }
}