package com.nemator.needle.fragments.haystack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.adapter.HaystackUserListCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UsersTaskResult;
import com.nemator.needle.models.vo.HaystackVO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackUsersTabFragment extends Fragment{
    public static final String TAG = "HaystackUsersTab";

    private View rootView;
    private RecyclerView listView;

    private HaystackVO haystack;
    private HaystackUserListCardAdapter userListAdapter;
    private GridLayoutManager layoutManager;

    public HaystackUsersTabFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_users_tab, container, false);

        listView =  (RecyclerView) rootView.findViewById(R.id.userList);
        int spanCount = 2;//TODO : define span depending on screen width
        layoutManager = new GridLayoutManager(getContext(), spanCount);
        listView.setLayoutManager(layoutManager);

        haystack = ((HaystackActivity) getActivity()).getHaystack();
        userListAdapter = new HaystackUserListCardAdapter(haystack.getUsers(), getActivity());
        listView.setAdapter(userListAdapter);

        fetchAllUsers();

        return rootView;
    }

    private void fetchAllUsers(){
        ApiClient.getInstance().fetchHaystackUsers(Needle.userModel.getUser(), haystack, usersFetchedCallback);
    }

    private Callback<UsersTaskResult> usersFetchedCallback = new Callback<UsersTaskResult>(){

        @Override
        public void onResponse(Call<UsersTaskResult> call, Response<UsersTaskResult> response) {
            UsersTaskResult result = response.body();
            Log.d(TAG, result.getUsers().size() + " users fetched");

            haystack.setUsers(result.getUsers());
            updateUserList();
        }

        @Override
        public void onFailure(Call<UsersTaskResult> call, Throwable t) {

        }
    };

    private void updateUserList(){
        userListAdapter = new HaystackUserListCardAdapter(haystack.getUsers(), getActivity());
        listView.setAdapter(userListAdapter);

        userListAdapter.notifyDataSetChanged();
        listView.invalidate();
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
        updateUserList();
    }
}