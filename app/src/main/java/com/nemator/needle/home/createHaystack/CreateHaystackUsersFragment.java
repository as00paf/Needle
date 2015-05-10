package com.nemator.needle.home.createHaystack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nemator.needle.R;
import com.nemator.needle.haystack.HaystackUserListAdapter;
import com.nemator.needle.haystack.task.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.haystack.task.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.haystack.task.retrieveUsers.RetrieveUsersTask;
import com.nemator.needle.home.HaystackListCardAdapter;
import com.nemator.needle.models.Haystack;
import com.nemator.needle.models.User;
import com.quinny898.library.persistentsearch.SearchBox;

import java.util.ArrayList;

public class CreateHaystackUsersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RetrieveUsersTask.RetrieveUsersResponseHandler{

    public static final String TAG = "CreateHaystackUsersFragment";

    //Children
    private RecyclerView mRecyclerView;
    private CreateHaystackUserListCardAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeLayout;
    private View rootView;
    private SearchBox searchBox;

    //Data
    private ArrayList<User> dataList;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
        }

        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_users, container, false);

        //Search Box
        searchBox = (SearchBox) rootView.findViewById(R.id.create_haystack_users_search_box);
        searchBox.enableVoiceRecognition(this);
        searchBox.setLogoText(getString(R.string.search_for_friends));
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results

                mAdapter.setFilter(searchBox.getSearchText());
            }

            @Override
            public void onSearch(String searchTerm) {
                mAdapter.setFilter(searchTerm);
            }

            @Override
            public void onSearchCleared() {
                mAdapter.flushFilter();
            }
        });

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_haystack_user, menu);
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

    //Actions
    private void fetchAllUsers(){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(getUserId());
        params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_ALL_USERS;

        try{
            RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
            task.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //Response Handlers
    public void onUsersRetrieved(RetrieveUsersResult result){
        dataList = result.userList;
        updateUserList();
    }

    private void updateUserList(){
        mAdapter = new CreateHaystackUserListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();

        swipeLayout.setRefreshing(false);
    }

    //Utils
    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    public ArrayList<User> getSelectedUsers(){
        return mAdapter.getSelectedItems();
    }
}
