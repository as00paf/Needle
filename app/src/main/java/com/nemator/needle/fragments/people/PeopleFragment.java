package com.nemator.needle.fragments.people;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.FriendsPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeopleFragment extends Fragment {
    public static final String TAG = "PeopleFragment";

    //Views
    private View rootView;
    private ViewPager viewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private FloatingActionButton fab;

    //Objects
    private FriendsPagerAdapter pagerAdapter;

    //Data
    public ArrayList<UserVO> friends = new ArrayList<>();
    public ArrayList<UserVO> friendRequests = new ArrayList<>();
    public ArrayList<UserVO> sentFriendRequests = new ArrayList<>();

    private long lastUpdate;

    public PeopleFragment() {
    }

    public static PeopleFragment newInstance() {
        PeopleFragment fragment = new PeopleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            FriendsTabFragment friendsTab = pagerAdapter.getFriendsFragment();
            FriendsTabFragment friendRequestsTab = pagerAdapter.getFriendRequestsFragment();
            FriendsTabFragment sentRequestsTab = pagerAdapter.getSentFriendRequestsFragment();

            friends = savedInstanceState.getParcelableArrayList("friends");
            friendRequests = savedInstanceState.getParcelableArrayList("friendRequests");
            sentFriendRequests = savedInstanceState.getParcelableArrayList("sentFriendRequests");

            if(friendRequests != null && getActivity() != null){
                ((HomeActivity) getActivity()).setFriendRequestsCount(friendRequests.size());
            }

            if(friendsTab != null) friendsTab.updateFriendsList(friends);
            if(friendRequestsTab != null) friendRequestsTab.updateFriendsList(friendRequests);
            if(sentRequestsTab != null) sentRequestsTab.updateFriendsList(sentFriendRequests);
        }

        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TODO : use constants
        outState.putParcelableArrayList("friends", friends);
        outState.putParcelableArrayList("friendRequests", friendRequests);
        outState.putParcelableArrayList("sentFriendRequests", sentFriendRequests);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_people_list, container, false);

            //FAB
            fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Find friends
                   /* Intent intent = new Intent(getContext(), CreateNeedleActivity.class);
                    startActivity(intent);*/
                }
            });

            //View pager
            pagerAdapter = new FriendsPagerAdapter(getActivity().getSupportFragmentManager(), this);
            viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
            viewPager.setOffscreenPageLimit(3);
            viewPager.setAdapter(pagerAdapter);

            //Tabs
            mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
            mSlidingTabLayout.setDistributeEvenly(true);
            mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

            mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(getContext(), android.R.color.white));
            mSlidingTabLayout.setViewPager(viewPager);
            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    switch(position){
                        case 0:

                            break;
                        case 1:

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

    public void fetchFriends(Boolean force){
        long now = new Date().getTime();

        if(!force){
            if(now < lastUpdate + 5000){
                return;
            }
        }

        lastUpdate = now;

        ApiClient.getInstance().getFriends(new Callback<FriendsResult>() {
            @Override
            public void onResponse(Call<FriendsResult> call, Response<FriendsResult> response) {
                FriendsTabFragment friendsTab = pagerAdapter.getFriendsFragment();
                FriendsTabFragment friendRequestsTab = pagerAdapter.getFriendRequestsFragment();
                FriendsTabFragment sentRequestsTab = pagerAdapter.getSentFriendRequestsFragment();

                if(friendsTab != null) friendsTab.setRefreshing(false);
                if(friendRequestsTab != null) friendRequestsTab.setRefreshing(false);
                if(sentRequestsTab != null) sentRequestsTab.setRefreshing(false);

                FriendsResult result = response.body();
                if(result.getSuccessCode() == 1){
                    friends = result.getFriends();
                    friendRequests = result.getReceivedFriendRequests();
                    sentFriendRequests = result.getSentFriendRequests();

                    if(friendRequests != null && getActivity() != null){
                        Log.d(TAG, friendRequests.size() + " Friend Requests fetched !");
                        ((HomeActivity) getActivity()).setFriendRequestsCount(friendRequests.size());
                    }

                    Log.d(TAG, friends.size() + " Friends fetched !");

                    if(friendsTab != null) friendsTab.updateFriendsList(friends);
                    if(friendRequestsTab != null) friendRequestsTab.updateFriendsList(friendRequests);
                    if(sentRequestsTab != null) sentRequestsTab.updateFriendsList(sentFriendRequests);
                }else{
                    Log.e(TAG, "Could not fetch location sharings. Error : " + result.getMessage());
                    Toast.makeText(getActivity(), R.string.fetch_needles_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendsResult> call, Throwable t) {
                FriendsTabFragment friendsTab = pagerAdapter.getFriendsFragment();
                FriendsTabFragment friendRequestsTab = pagerAdapter.getFriendRequestsFragment();
                FriendsTabFragment sentRequestsTab = pagerAdapter.getSentFriendRequestsFragment();

                friendsTab.getRefreshLayout().setRefreshing(false);
                friendRequestsTab.getRefreshLayout().setRefreshing(false);
                sentRequestsTab.getRefreshLayout().setRefreshing(false);

                Log.e(TAG, "Could not fetch friends. Error : " + t.getMessage());
                Toast.makeText(getContext(), R.string.fetch_needles_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchFriends(true);
    }

}
