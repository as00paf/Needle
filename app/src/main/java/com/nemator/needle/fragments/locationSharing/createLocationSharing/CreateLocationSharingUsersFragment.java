package com.nemator.needle.fragments.locationSharing.createLocationSharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.UserListCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackBaseFragment;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateLocationSharingUsersFragment extends CreateHaystackBaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String TAG = "UsersFragment";

    //Children
    private RecyclerView mRecyclerView;
    private UserListCardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<UserVO> usersList;
    private int userId = -1;

    public static CreateLocationSharingUsersFragment newInstance() {
        CreateLocationSharingUsersFragment fragment = new CreateLocationSharingUsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateLocationSharingUsersFragment() {
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
        fetchAllUsers();
    }

    @Override
    public void onRefresh(){
        fetchAllUsers();
    }

    public void closeSearchResults(){
        //searchBox.toggleSearch();
    }

    //Actions
    private void fetchAllUsers(){
        ApiClient.getInstance().fetchAllUsers(Needle.userModel.getUserId(), new Callback<UsersResult>() {
            @Override
            public void onResponse(Call<UsersResult> call, Response<UsersResult> response) {
                UsersResult result = response.body();
                usersList = result.getUsers();
                updateUserList();
            }

            @Override
            public void onFailure(Call<UsersResult> call, Throwable t) {
                Log.d(TAG, "Retrieving users failed ! Error : " + t.getMessage());

                usersList = new ArrayList<UserVO>();
                updateUserList();
            }
        });
    }

    private void updateUserList(){
        mAdapter = new UserListCardAdapter(usersList, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();

        swipeLayout.setRefreshing(false);
    }

    public ArrayList<UserVO> getSelectedUsers(){
        return mAdapter.getSelectedItems();
    }

    public UserVO getSelectedUser(){
        return mAdapter.getSelectedItems().get(0);
    }
}
