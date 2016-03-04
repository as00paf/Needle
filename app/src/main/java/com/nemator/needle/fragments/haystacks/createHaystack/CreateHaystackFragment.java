package com.nemator.needle.fragments.haystacks.createHaystack;

import android.animation.Animator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HomeActivity;
import com.nemator.needle.adapter.CreateHaystackPagerAdapter;
import com.nemator.needle.fragments.SearchFragment;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppState;
import com.nemator.needle.utils.CameraUtils;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

public class CreateHaystackFragment extends CreateHaystackBaseFragment implements CreateHaystackGeneralInfosFragment.OnPrivacySettingsUpdatedListener{
    public static final String TAG = "CreateHaystackFragment";

    //Activity Results
    public static int TAKE_PICTURE = 226;

    //View
    private View rootView;
    private Button nextButton, backButton;
    private FloatingActionButton fab;
    private CirclePageIndicator viewPagerIndicator;

    //Fragments
    public CreateHaystackGeneralInfosFragment mCreateHaystackGenInfosFragment;
    public CreateHaystackMapFragment mCreateHaystackMapFragment;
    public CreateHaystackUsersFragment mCreateHaystackUsersFragment;

    //Data
    private String userName;
    private HaystackVO haystack;
    private int userId = -1;
    private ArrayList<UserVO> userList = new ArrayList<UserVO>();

    private ViewPager createHaystackViewPager;
    private CreateHaystackPagerAdapter mCreateHaystackPagerAdapter;
    private Boolean isPublic = false;
    private OnActivityStateChangeListener stateChangeCallback;
    private SearchView searchView;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private SearchFragment searchFragment;

    public static CreateHaystackFragment newInstance() {
        CreateHaystackFragment fragment = new CreateHaystackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = Needle.navigationController;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(apiConnectedReceiver, new IntentFilter(AppConstants.SOCIAL_NETWORKS_INITIALIZED));
    }

    private BroadcastReceiver apiConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            localBroadcastManager.unregisterReceiver(this);

            initPermissionsAndServices();
        }
    };

    private void initPermissionsAndServices() {
        if(Needle.gcmController.checkPlayServices()){
            Needle.googleApiController.checkLocationSettings();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack, container, false);

        //ViewPager
        mCreateHaystackPagerAdapter = new CreateHaystackPagerAdapter(getActivity().getSupportFragmentManager(), getActivity());
        createHaystackViewPager = (ViewPager) rootView.findViewById(R.id.create_haystack_view_pager);
        createHaystackViewPager.setAdapter(mCreateHaystackPagerAdapter);

        //ViewPagerIndicator
        viewPagerIndicator  = (CirclePageIndicator) rootView.findViewById(R.id.view_pager_indicator);
        viewPagerIndicator.setViewPager(createHaystackViewPager);
        viewPagerIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //Back/Next Button
                backButton.setEnabled((position == 1 || position == 2));
                nextButton.setEnabled((position == 0 || (position == 1 && isPublic == false)));

                //FAB & Back-Stack
                switch (position){
                    case 0:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_photo_camera_black_24dp));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_GENERAL_INFOS);
                        break;
                    case 1:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_action_location_found));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_MAP);
                        break;
                    case 2:
                        fab.setImageDrawable(getResources().getDrawable( R.drawable.ic_person_add_black_24dp));
                        stateChangeCallback.onStateChange(AppState.CREATE_HAYSTACK_USERS);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPagerIndicator.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "here");

                if(v instanceof MapView){
                    return true;
                }

                return false;
            }
        });

        //FAB
        fab = (FloatingActionButton) rootView.findViewById(R.id.new_haystack_photo_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = createHaystackViewPager.getCurrentItem();
                switch (position) {
                    case 0:
                        //Take Picture
                        Intent intent = CameraUtils.getImageCaptureIntent();
                        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                    case 1:
                        //Focus Camera on current position
                        ((CreateHaystackMapFragment) getFragmentManager().getFragments().get(4)).getMapController().focusOnMyPosition();
                        break;
                    case 2:
                        //Invite Friend

                        break;
                }

            }
        });

        //Bottom Sheet Buttons
        nextButton = (Button) rootView.findViewById(R.id.create_haystack_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() + 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                updateButtonsState();
            }
        });

        backButton = (Button) rootView.findViewById(R.id.create_haystack_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIndex = createHaystackViewPager.getCurrentItem() - 1;
                createHaystackViewPager.setCurrentItem(newIndex);
                updateButtonsState();
            }
        });

        String title = getResources().getString(R.string.create_haystack);
        Needle.navigationController.setActionBarTitle(title);

        //Search
        searchFragment = (SearchFragment) ((HomeActivity) getActivity()).getSupportFragmentManager().findFragmentById(R.id.searchFragment);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CreateHaystackFragment.TAKE_PICTURE && resultCode== getActivity().RESULT_OK && data != null){
            Bundle extras = data.getExtras();

            Bitmap bitmap = (Bitmap) extras.get("data");
            //Rotate if portrait
            if(bitmap.getHeight() > bitmap.getWidth()){
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            CreateHaystackGeneralInfosFragment fragment = (CreateHaystackGeneralInfosFragment) mCreateHaystackPagerAdapter.getFragmentAt(0);
            fragment.updatePhoto(bitmap);
        }
    }


    private void initializeBroadcastListener() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchStateListener,
                new IntentFilter(getString(R.string.action_search_started)));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchStateListener,
                new IntentFilter(getString(R.string.action_search_finished)));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchItemListener,
                new IntentFilter(getString(R.string.action_search_item_selected)));

    }

    private BroadcastReceiver searchStateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Boolean showProgressBar = action == getString(R.string.action_search_started);
            //TODO : add progress bar
            //progressBar.setVisibility(showProgressBar ? View.VISIBLE : View.INVISIBLE);
        }
    };

    private BroadcastReceiver searchItemListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            LatLng location = intent.getParcelableExtra(getString(R.string.location));
            if(location != null){
                closeSearchMenu();
                //TODO : move camera if necessary
                //mapController.cameraController.unfocus(location);
                //mapController.getRoadSidesNearLocation(location);
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int position = createHaystackViewPager.getCurrentItem();
        switch(position){
            case 0:
                super.onCreateOptionsMenu(menu, inflater);
                break;
            case 1:
                if(isPublic){
                    inflater.inflate(R.menu.menu_create_haystack_done, menu);
                }else{
                    inflater.inflate(R.menu.menu_create_haystack_search, menu);
                }

                setupSearchView(menu);
                break;
            case 2:
                inflater.inflate(R.menu.menu_create_haystack_done, menu);
                setupSearchView(menu);
                break;
        }
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

                searchFragment.hide();
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

        searchFragment.show(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                searchView.clearFocus();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //Show keyboard
                searchView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text), InputMethodManager.SHOW_FORCED);

                animation.removeListener(this);
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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

        searchFragment.hide();
        isSearchOpened = false;
    }

    private void updateButtonsState(){
        int currentItemIndex = createHaystackViewPager.getCurrentItem();
        backButton.setEnabled(currentItemIndex != 0);
        nextButton.setEnabled((currentItemIndex == 0 || (currentItemIndex == 1 && isPublic == false)));
    }

    private void doSearch(String query) {
        Log.d(TAG, "doSearch::query : " + query);

        searchFragment.guessLocation(query, mCreateHaystackMapFragment.getCameraTargetBounds());
    }

    @Override
    public void onPrivacySettingsChanged(Boolean isPublic) {
        mCreateHaystackPagerAdapter.setIsPublic(isPublic);
        viewPagerIndicator.notifyDataSetChanged();

        this.isPublic = isPublic;
    }

    public void goToPage(int page){
        createHaystackViewPager.setCurrentItem(page);
    }
}
