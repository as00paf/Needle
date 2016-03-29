package com.nemator.needle.fragments.haystacks.createHaystack;

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
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UsersResult;
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
    private ArrayList<UserVO> usersList;
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

        //Search Box
        /*
        searchListener = new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
                stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_USERS_SEARCH_OPEN);

                if(searchBox.getSearchables().size() == 0){
                    addFriendsSuggestion();
                }
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
        };
        searchBox.setSearchListener(searchListener);
*/
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

    private void addFriendsSuggestion(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.size() > 0){
                searchBox.clearSearchable();
                searchBox.setSearchString(matches.get(0));

                //Match voice search with users
                for (int i = 0; i < matches.size(); i++) {
                    String currentMatch = matches.get(i);
                    if(usersList != null && usersList.size() > 0 ){
                        for (int j = 0; j < usersList.size(); j++) {
                            String currentUserName = usersList.get(j).getName().toLowerCase();
                            if(currentUserName.contains(currentMatch)){
                                SearchResult newSearchResult = new SearchResult(usersList.get(j).getName(), getResources().getDrawable(R.drawable.person_placeholder_24));
                                Boolean alreadyAdded = false;
                                for (int k = 0; k < searchBox.getSearchables().size(); k++) {
                                    SearchResult oldResult = searchBox.getSearchables().get(k);
                                    if(oldResult.description.equals(newSearchResult.description)){
                                        alreadyAdded = true;
                                    }
                                }

                                if(!alreadyAdded){
                                    searchBox.addSearchable(newSearchResult);
                                }
                            }
                        }

                        searchListener.onSearchTermChanged();
                        searchBox.toggleSearch();
                    }
                }
            }
        }*/
        super.onActivityResult(requestCode, resultCode, data);
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
        mAdapter = new UserCardAdapter(usersList, getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();

        swipeLayout.setRefreshing(false);
    }

    public ArrayList<UserVO> getSelectedUsers(){
        return mAdapter.getSelectedItems();
    }
}
