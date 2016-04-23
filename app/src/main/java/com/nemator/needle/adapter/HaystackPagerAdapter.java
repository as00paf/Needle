package com.nemator.needle.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.HaystackListTabFragment;

public class HaystackPagerAdapter extends FragmentStatePagerAdapter {

    //Tabs
    private HaystackListTabFragment publicHaystackListFragment;
    private HaystackListTabFragment privateHaystackListFragment;
    private HaystackListTabFragment ownedHaystackListFragment;

    private HaystackListTabFragment.HaystackListFragmentInteractionListener listener;
    private Context context;

    public HaystackPagerAdapter(FragmentManager fm, Context context, HaystackListTabFragment.HaystackListFragmentInteractionListener listener) {
        super(fm);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public Fragment getItem(int position) {
        HaystackListTabFragment fragment  = HaystackListTabFragment.newInstance(listener);

        Bundle args = new Bundle();
        switch(position){
            case 0:
                publicHaystackListFragment = fragment;
                break;
            case 1:
                privateHaystackListFragment = fragment;
                break;
            case 2:
                ownedHaystackListFragment = fragment;
                break;
        }

        args.putInt("type", position);
        fragment.setArguments(args);

        return fragment;
    }

    public HaystackListTabFragment getPublicHaystackListFragment(){
        return publicHaystackListFragment;
    }

    public HaystackListTabFragment getPrivateHaystackListFragment(){
        return privateHaystackListFragment;
    }

    public HaystackListTabFragment getOwnedHaystackListFragment(){
        return ownedHaystackListFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
            case 0:
                title = context.getString(R.string.publicHeader);
                break;
            case 1:
                title = context.getString(R.string.privateHeader);
                break;
            case 2:
                title = context.getString(R.string.ownedHeader);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }


}