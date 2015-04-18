package com.nemator.needle.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.home.createHaystack.CreateHaystackFragment;
import com.nemator.needle.R;

import com.shamanland.fab.FloatingActionButton;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class HaystackListFragment extends Fragment{
    private static final String TAG = "HaystackListFragment";

    private View rootView;
    private FloatingActionButton fab = null;

    private ViewPager haystackListViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private HaystackListPagerAdapter mHaystackListPagerAdapter;

    public static HaystackListFragment newInstance() {
        HaystackListFragment fragment = new HaystackListFragment();
        return fragment;
    }

    public HaystackListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_haystack_list, container, false);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setColor(getResources().getColor(R.color.primary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MaterialNavigationDrawer) getActivity()).setFragment(CreateHaystackFragment.newInstance(), getString(R.string.create_haystack));
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

        return rootView;
    }
}
