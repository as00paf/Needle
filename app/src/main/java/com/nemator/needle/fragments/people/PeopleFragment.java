package com.nemator.needle.fragments.people;

import android.animation.Animator;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.People;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.PeoplePagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.models.vo.FriendVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.SphericalUtil;

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
    private Menu menu;
    private SearchView searchView;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;

    //Objects
    private PeoplePagerAdapter pagerAdapter;

    //Data
    public ArrayList<UserVO> friends = new ArrayList<>();
    public ArrayList<FriendVO> friendRequests = new ArrayList<>();
    public ArrayList<FriendVO> sentFriendRequests = new ArrayList<>();

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

            friends = savedInstanceState.getParcelableArrayList("friends");
            friendRequests = savedInstanceState.getParcelableArrayList("friendRequests");
            sentFriendRequests = savedInstanceState.getParcelableArrayList("sentFriendRequests");

            if(friendRequests != null && getActivity() != null){
                ((HomeActivity) getActivity()).setFriendRequestsCount(friendRequests.size());
            }

            if(friendsTab != null) friendsTab.updateFriendsList(friends);
            if(friendRequestsTab != null) friendRequestsTab.updateFriendshipList(friendRequests, sentFriendRequests);
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
            pagerAdapter = new PeoplePagerAdapter(getActivity().getSupportFragmentManager(), this);
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

            setHasOptionsMenu(true);
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
                FriendsTabFragment discoverTab = pagerAdapter.getDiscoverFragment();

                if(friendsTab != null) friendsTab.setRefreshing(false);
                if(friendRequestsTab != null) friendRequestsTab.setRefreshing(false);

                FriendsResult result = response.body();
                if(result.getSuccessCode() == 1){
                    friends = result.getFriends();
                    friendRequests = result.getReceivedFriendRequests();
                    sentFriendRequests = result.getSentFriendRequests();

                    if(friendRequests != null && getActivity() != null){
                        Log.d(TAG, friendRequests.size() + " Friend Requests fetched !");
                        ((HomeActivity) getActivity()).setFriendRequestsCount(friendRequests.size());
                    }

                    if(friends != null) Log.d(TAG, friends.size() + " Friends fetched !");

                    if(friendsTab != null) friendsTab.updateFriendsList(friends);
                    if(friendRequestsTab != null) friendRequestsTab.updateFriendshipList(friendRequests, sentFriendRequests);
                    if(discoverTab != null) {
                        discoverTab.setFriends(friends);
                        discoverTab.setReceivedRequests(friendRequests);
                        discoverTab.fetchDiscoverUsers();
                    }
                }else{
                    Log.e(TAG, "Could not fetch location sharings. Error : " + result.getMessage());
                    Toast.makeText(getActivity(), R.string.fetch_needles_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendsResult> call, Throwable t) {
                FriendsTabFragment friendsTab = pagerAdapter.getFriendsFragment();
                FriendsTabFragment friendRequestsTab = pagerAdapter.getFriendRequestsFragment();
                FriendsTabFragment sentRequestsTab = pagerAdapter.getDiscoverFragment();

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

        if(pagerAdapter != null && pagerAdapter.getDiscoverFragment() != null){
            pagerAdapter.getDiscoverFragment().fetchDiscoverUsers();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        if(viewPager.getCurrentItem() != PeoplePagerAdapter.REQUESTS ){
            inflater.inflate(R.menu.menu_default_search, menu);
            setupSearchView(menu);
        }else{
            inflater.inflate(R.menu.menu_default, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_search:
                handleMenuSearch();
                break;
            case android.R.id.home:
                if(isSearchOpened) {
                    closeSearchMenu();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupSearchView(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchAction = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        SearchManager searchManager = (SearchManager) getContext().getSystemService(getContext().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
            }
        });

        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                ActionBar action = ((HomeActivity) getActivity()).getSupportActionBar();
                mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white_24dp));
                action.setTitle(getResources().getString(R.string.app_name));
                action.setDisplayShowCustomEnabled(false);
                action.setDisplayShowTitleEnabled(true);
                action.setDisplayShowHomeEnabled(true);

                pagerAdapter.getDiscoverFragment().clearSearch();
                isSearchOpened = false;

                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;  // Return true to expand action view
            }
        });

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setMaxWidth(2000);
    }

    private void handleMenuSearch() {
        if(isSearchOpened){
            closeSearchMenu();
        } else {
            openSearchMenu();
        }
    }

    private void openSearchMenu(){
        ActionBar action = ((HomeActivity) getActivity()).getSupportActionBar();
        action.setCustomView(R.layout.search_bar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                doSearch(newText);
                return false;
            }
        });

        //add the close icon
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
        action.setTitle(getResources().getString(R.string.app_name));

        //Show keyboard
        searchView.requestFocus();

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text), InputMethodManager.SHOW_FORCED);

        isSearchOpened = true;
    }

    private void closeSearchMenu(){
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        ActionBar action = ((HomeActivity) getActivity()).getSupportActionBar();
        mSearchAction.collapseActionView();
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white_24dp));
        action.setTitle(getResources().getString(R.string.app_name));
        action.setDisplayShowCustomEnabled(false);
        action.setDisplayShowTitleEnabled(true);
        action.setDisplayShowHomeEnabled(true);

        pagerAdapter.getDiscoverFragment().clearSearch();
        isSearchOpened = false;
    }

    private void doSearch(String query) {
        Log.d(TAG, "doSearch::query : " + query);

        if(viewPager.getCurrentItem() == PeoplePagerAdapter.DISCOVER){
            pagerAdapter.getDiscoverFragment().searchForUser(query);
        }else{
            pagerAdapter.getFriendsFragment().searchForUser(query);
        }
    }

}
