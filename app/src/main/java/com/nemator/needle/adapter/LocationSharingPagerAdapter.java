package com.nemator.needle.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.locationSharing.LocationSharingListTabFragment;

public class LocationSharingPagerAdapter extends FragmentStatePagerAdapter {
    private LocationSharingListTabFragment receivedFragment;
    private LocationSharingListTabFragment sentFragment;

    private Fragment fragment;

    public LocationSharingPagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        LocationSharingListTabFragment fragment;

        fragment = new LocationSharingListTabFragment();

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

    public LocationSharingListTabFragment getReceivedFragment(){
        return receivedFragment;
    }

    public LocationSharingListTabFragment getSentFragment(){
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