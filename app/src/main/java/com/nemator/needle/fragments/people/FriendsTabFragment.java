package com.nemator.needle.fragments.people;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.FriendRequestAdapter;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.models.vo.FriendVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsTabFragment extends Fragment{
    private static final String TAG = "ListTabFragment";

    //Views
    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<UserVO> friends, potentialFriends;
    private ArrayList<FriendVO> receivedRequests, sentRequests;
    private int type;

    //Objects
    private RecyclerView.Adapter listAdapter;
    private GridLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            type = args.getInt(AppConstants.TAG_TYPE, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_friends_list_tab, container, false);

            //Recycler View
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);

            layoutManager = new GridLayoutManager(getActivity(), 2);

            listAdapter = new UserCardAdapter(getActivity(), null, UserCardAdapter.Type.SHOW_PROFILE);
            mRecyclerView.setAdapter(listAdapter);

            //Swipe To Refresh
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshList();
                }
            });
            swipeLayout.setRefreshing(true);

            setHasOptionsMenu(true);
        }

        return rootView;
    }

    public void fetchDiscoverUsers() {
        ArrayList<UserVO> excepted = new ArrayList<>();
        excepted.add(Needle.userModel.getUser());//Me
        if(receivedRequests != null){
            Iterator<FriendVO> iterator = receivedRequests.iterator();
            while(iterator.hasNext()){
                excepted.add(iterator.next().getUser());//Already received requests
            }
        }

        if(friends != null){
            excepted.addAll(friends);//Friends
        }

        if(excepted.size() > 0){
            ApiClient.getInstance().getPotentialFriends(excepted, potentialFriendsCallback);
        }
    }

    private Callback<FriendsResult> potentialFriendsCallback = new Callback<FriendsResult>() {
        @Override
        public void onResponse(Call<FriendsResult> call, Response<FriendsResult> response) {
            FriendsResult result = response.body();

            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Retrieved potential friends");

                potentialFriends = result.getFriends();
            }else{
                Log.d(TAG, "Could not retrieve potential friends. Error : " + result.getMessage());
            }

            listAdapter = new UserCardAdapter(getActivity(), potentialFriends, UserCardAdapter.Type.SHOW_PROFILE);

            if(mRecyclerView != null){
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return listAdapter.getItemViewType(position) == UserCardAdapter.TYPE_EMPTY ? 2 : 1;
                    }
                });
                mRecyclerView.setLayoutManager(layoutManager);

                mRecyclerView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailure(Call<FriendsResult> call, Throwable t) {
            Log.d(TAG, "Could not retrieve potential friends. Error : " + t.getMessage());
        }
    };

    public void updateFriendsList(ArrayList<UserVO> data){
        this.friends = data;
        listAdapter = new UserCardAdapter(getActivity(), friends, UserCardAdapter.Type.SHOW_PROFILE);

        if(mRecyclerView != null){
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return listAdapter.getItemViewType(position) == UserCardAdapter.TYPE_EMPTY ? 2 : 1;
                }
            });
            mRecyclerView.setLayoutManager(layoutManager);

            mRecyclerView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
    }

    public void updateFriendshipList(ArrayList<FriendVO> receivedRequests, ArrayList<FriendVO> sentRequests){
        this.receivedRequests = receivedRequests;
        this.sentRequests = sentRequests;
        listAdapter = new FriendRequestAdapter(getActivity(), receivedRequests, sentRequests);

        if(mRecyclerView != null){
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 2;
                }
            });
            mRecyclerView.setLayoutManager(layoutManager);

            mRecyclerView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }

    public void setRefreshing(boolean value){
        if(swipeLayout!=null) swipeLayout.setRefreshing(value);
    }

    public ArrayList<UserVO> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<UserVO> friends) {
        this.friends = friends;
    }

    public ArrayList<FriendVO> getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(ArrayList<FriendVO> receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    //Handlers
    public void onRefreshList() {
        if(type == 2){
            fetchDiscoverUsers();
        }else{
            Needle.navigationController.refreshFriendsList();
        }
    }

    public void searchForUser(String query) {
        ((UserCardAdapter) listAdapter).setFilter(query);
    }

    public void clearSearch() {
        ((UserCardAdapter) listAdapter).flushFilter();
    }
}
