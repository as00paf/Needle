package com.nemator.needle.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.needle.NeedleListTabFragment;
import com.nemator.needle.fragments.people.FriendsTabFragment;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class FriendsPagerAdapter extends FragmentStatePagerAdapter {
    private FriendsTabFragment friendsFragment;
    private FriendsTabFragment friendRequestsFragment;
    private FriendsTabFragment sentFriendRequestsFragment;

    private Fragment fragment;

    public FriendsPagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        FriendsTabFragment fragment = null;
        fragment = new FriendsTabFragment();

        switch (position){
            case 0:
                if(friendsFragment == null) friendsFragment = new FriendsTabFragment();
                fragment = friendsFragment;
                break;
            case 1:
                if(friendRequestsFragment == null) friendRequestsFragment = new FriendsTabFragment();
                fragment = friendRequestsFragment;
                break;
            case 2:
                if(sentFriendRequestsFragment == null) sentFriendRequestsFragment = new FriendsTabFragment();
                fragment = sentFriendRequestsFragment;
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

    public FriendsTabFragment getSentFriendRequestsFragment(){
        return sentFriendRequestsFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        switch (position){
            case 0:
                title = fragment.getString(R.string.friends);
                break;
            case 1:
                title = "Request";//fragment.getString(R.string.friend_requests);
                break;
            case 2:
                title = "Sent";//fragment.getString(R.string.sent_friend_requests);
                break;
        }

        return title;
    }

}