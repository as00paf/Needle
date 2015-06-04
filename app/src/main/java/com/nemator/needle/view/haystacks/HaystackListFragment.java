package com.nemator.needle.view.haystacks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.tasks.fetchHaystacks.FetchHaystacksParams;
import com.nemator.needle.tasks.fetchHaystacks.FetchHaystacksResult;
import com.nemator.needle.tasks.fetchHaystacks.FetchHaystacksTask;
import com.nemator.needle.utils.AppState;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;

public class HaystackListFragment extends Fragment implements FetchHaystacksTask.FetchHaystackResponseHandler, SwipeRefreshLayout.OnRefreshListener{
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
    private HaystackListFragmentInteractionListener fabListener;

    //Data
    public ArrayList<HaystackVO> publicHaystacks = null;
    public ArrayList<HaystackVO> privateHaystacks = null;

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
            stateChangeCallback = (OnActivityStateChangeListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_haystack_list, container, false);

            //FAB
            fabListener = ((MainActivity) getActivity());
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setColor(getResources().getColor(R.color.primary));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fabListener.onCreateHaystackFabTapped();
                }
            });
            fab.initBackground();

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

    public void fetchHaystacks(){
        FetchHaystacksParams params = new FetchHaystacksParams(getUserName(), String.valueOf(getUserId()), rootView.getContext());

        try{
            FetchHaystacksTask task = new FetchHaystacksTask(params, this);
            task.execute();
        }catch(Exception e){
            Log.e(TAG, "fetchHaystacks exception : " + e.toString());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchHaystacks();
    }

    @Override public void onRefresh() {
        fetchHaystacks();
    }

    public void onHaystackFetched(FetchHaystacksResult result){
        publicHaystacks = result.publicHaystackList;
        privateHaystacks = result.privateHaystackList;

        HaystackListTabFragment publicTab = mHaystackListPagerAdapter.getPublicHaystackListFragment();
        HaystackListTabFragment privateTab = mHaystackListPagerAdapter.getPrivateHaystackListFragment();

        publicTab.getRefreshLayout().setRefreshing(false);
        privateTab.getRefreshLayout().setRefreshing(false);

        //Show how many haystacks are available in the Nav Drawer
        int count = publicHaystacks.size() + privateHaystacks.size();
        ((MainActivity) getActivity()).setHaystacksCount(count);

        mHaystackListPagerAdapter.getPublicHaystackListFragment().updateHaystackList(publicHaystacks);
        mHaystackListPagerAdapter.getPrivateHaystackListFragment().updateHaystackList(privateHaystacks);
    }

    private int getUserId(){
        return ((MainActivity) getActivity()).getUserId();
    }

    private String getUserName(){
        return ((MainActivity) getActivity()).getUserName();
    }

    public void goToTab(int tab){
        haystackListViewPager.setCurrentItem(tab);
    }

    public interface HaystackListFragmentInteractionListener {
        void onCreateHaystackFabTapped();
        void onRefreshHaystackList();
    }
}