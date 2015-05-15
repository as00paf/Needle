package com.nemator.needle.view.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;

public class HaystackListPagerAdapter extends FragmentStatePagerAdapter {
    private HaystackListTabFragment publicHaystackListFragment;
    private HaystackListTabFragment privateHaystackListFragment;

    private Fragment fragment;

    public HaystackListPagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        HaystackListTabFragment fragment;

            /*switch (position){
                case 0://Public tab
                    fragment = getPublicHaystackListFragment();
                    break;
                case 1://Private tab
                    fragment = getPrivateHaystackListFragment();
                    break;
                default:
                    fragment = new HaystackListTabFragment();
                    break;
            }*/

        fragment = new HaystackListTabFragment();

        Bundle args = new Bundle();
        Boolean isPublic = (position == 0);
        args.putBoolean("isPublic", isPublic);
        fragment.setArguments(args);

        return fragment;
    }

    public HaystackListTabFragment getPublicHaystackListFragment(){
        if(publicHaystackListFragment == null) {
            publicHaystackListFragment = new HaystackListTabFragment();
        }

        return publicHaystackListFragment;
    }

    public HaystackListTabFragment getPrivateHaystackListFragment(){
        if(privateHaystackListFragment == null) {
            privateHaystackListFragment = new HaystackListTabFragment();
        }

        return privateHaystackListFragment;
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
                title = fragment.getString(R.string.publicHeader);
                break;
            case 1:
                title = fragment.getString(R.string.privateHeader);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }
}