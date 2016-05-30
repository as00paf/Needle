package com.nemator.needle.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.people.FriendsTabFragment;

public class PeoplePagerAdapter extends FragmentStatePagerAdapter {

    public static final int FRIENDS = 0;
    public static final int REQUESTS = 1;
    public static final int DISCOVER = 2;

    private FriendsTabFragment friendsFragment;
    private FriendsTabFragment friendRequestsFragment;
    private FriendsTabFragment discoverFragment;

    private Fragment fragment;

    public PeoplePagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        FriendsTabFragment fragment = null;
        fragment = new FriendsTabFragment();

        switch (position){
            case FRIENDS:
                if(friendsFragment == null) friendsFragment = new FriendsTabFragment();
                fragment = friendsFragment;
                break;
            case REQUESTS:
                if(friendRequestsFragment == null) friendRequestsFragment = new FriendsTabFragment();
                fragment = friendRequestsFragment;
                break;
            case DISCOVER:
                if(discoverFragment == null) discoverFragment = new FriendsTabFragment();
                fragment = discoverFragment;
                break;
        }

        Bundle args = new Bundle();
        args.putInt("type", position);
        fragment.setArguments(args);

        return fragment;
    }

    public FriendsTabFragment getFriendsFragment(){
        return friendsFragment;
    }

    public FriendsTabFragment getFriendRequestsFragment(){
        return friendRequestsFragment;
    }

    public FriendsTabFragment getDiscoverFragment(){
        return discoverFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        switch (position){
            case FRIENDS:
                title = fragment.getString(R.string.friends);
                break;
            case REQUESTS:
                title = fragment.getString(R.string.friend_requests);
                break;
            case DISCOVER:
                title = fragment.getString(R.string.discover);
                break;
        }

        return title;
    }

}