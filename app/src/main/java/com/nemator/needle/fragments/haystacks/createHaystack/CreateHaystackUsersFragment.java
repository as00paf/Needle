package com.nemator.needle.fragments.haystacks.createHaystack;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateHaystackUsersFragment extends CreateHaystackBaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String TAG = "CreateHaystackUsers";

    //Children
    private RecyclerView mRecyclerView;
    private UserCardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<UserVO> friends;
    private int userId = -1;

    public static CreateHaystackUsersFragment newInstance() {
        CreateHaystackUsersFragment fragment = new CreateHaystackUsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackUsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_users, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.create_haystack_users_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.create_haystack_swipe_container);
        swipeLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        getFriends();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.setFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.setFilter(newText);
                return false;
            }
        });
    }

    @Override
    public void onRefresh(){
        getFriends();
    }

    //Actions
    private void getFriends(){
        ApiClient.getInstance().getFriends(Needle.userModel.getUserId(), new Callback<FriendsResult>() {
            @Override
            public void onResponse(Call<FriendsResult> call, Response<FriendsResult> response) {
                FriendsResult result = response.body();
                friends = result.getFriends();
                updateFriendList();
            }

            @Override
            public void onFailure(Call<FriendsResult> call, Throwable t) {
                Log.d(TAG, "Retrieving friends failed ! Error : " + t.getMessage());

                friends = new ArrayList<UserVO>();
                updateFriendList();
            }
        });
    }

    private void updateFriendList(){
        mAdapter = new UserCardAdapter(getActivity(), friends, UserCardAdapter.Type.MULTI_SELECT);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();

        swipeLayout.setRefreshing(false);
    }

    public ArrayList<UserVO> getSelectedUsers(){
        return mAdapter.getSelectedItems();
    }
}
