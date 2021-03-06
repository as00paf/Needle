package com.nemator.needle.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.needle.NeedleListTabFragment;

public class NeedlePagerAdapter extends FragmentStatePagerAdapter {
    private NeedleListTabFragment receivedFragment;
    private NeedleListTabFragment sentFragment;

    private Fragment fragment;

    public NeedlePagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        NeedleListTabFragment fragment = NeedleListTabFragment.newInstance();

        Bundle args = new Bundle();
        Boolean isReceived = (position == 0);

        if(isReceived){
            receivedFragment = fragment;
        }else{
            sentFragment = fragment;
        }

        args.putBoolean("isReceived", isReceived);
        fragment.setArguments(args);

        return fragment;
    }

    public NeedleListTabFragment getReceivedFragment(){
        return receivedFragment;
    }

    public NeedleListTabFragment getSentFragment(){
        return sentFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
            case 0:
                title = fragment.getString(R.string.receivedHeader);
                break;
            case 1:
                title = fragment.getString(R.string.sentHeader);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }
}