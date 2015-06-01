package com.nemator.needle.view.haystacks.createHaystack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

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
    private SearchBox.SearchListener searchListener;
    private OnActivityStateChangeListener stateChangeCallback;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = (OnActivityStateChangeListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
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
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == getActivity().RESULT_OK) {
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
                            String currentUserName = usersList.get(j).getUserName().toLowerCase();
                            if(currentUserName.contains(currentMatch)){
                                SearchResult newSearchResult = new SearchResult(usersList.get(j).getUserName(), getResources().getDrawable(R.drawable.person_placeholder_24));
                                Boolean alreadyAdded = false;
                                for (int k = 0; k < searchBox.getSearchables().size(); k++) {
                                    SearchResult oldResult = searchBox.getSearchables().get(k);
                                    if(oldResult.title.equals(newSearchResult.title)){
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
        }
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
        searchBox.toggleSearch();
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
        usersList = result.userList;
        updateUserList();
    }

    private void updateUserList(){
        mAdapter = new CreateHaystackUserListCardAdapter(usersList, rootView.getContext());
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

    public ArrayList<UserVO> getSelectedUsers(){
        return mAdapter.getSelectedItems();
    }
}
