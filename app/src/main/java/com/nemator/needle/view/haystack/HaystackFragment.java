package com.nemator.needle.view.haystack;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask;
import com.nemator.needle.utils.AppConstants;
import com.shamanland.fab.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class HaystackFragment extends Fragment {
    private static final String TAG = "HaystackFragment";

    private SlidingUpPanelLayout mLayout;
    private SlidingPanelPagerAdapter mSlidingPanelPagerAdapter;
    private  ViewPager slidingPanelViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private HaystackVO haystack;
    private boolean mIsOwner;

    public static HaystackFragment newInstance() {
        HaystackFragment fragment = new HaystackFragment();
        return fragment;
    }

    public HaystackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.TAG_IS_OWNER)) {
                mIsOwner = savedInstanceState.getBoolean(AppConstants.TAG_IS_OWNER);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (SlidingUpPanelLayout) inflater.inflate(R.layout.haystack_fragment, container, false);

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.setClipPanel(false);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded");

            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

        //Active Until Label
        String activeUntil = getResources().getString(R.string.activeUntil)+ " " + haystack.getTimeLimit();
        activeUntil = activeUntil.replace(" 00:00:00", "");
        activeUntil = activeUntil.replace(":00", "");

        TextView activeUntilLabel = (TextView) mLayout.findViewById(R.id.active_until_label);
        activeUntilLabel.setText(activeUntil);

        //View pager
        mSlidingPanelPagerAdapter = new SlidingPanelPagerAdapter(getActivity().getSupportFragmentManager());
        slidingPanelViewPager = (ViewPager) mLayout.findViewById(R.id.slidingPanelViewPager);
        slidingPanelViewPager.setAdapter(mSlidingPanelPagerAdapter);

        //Tabs
        mSlidingTabLayout = (SlidingTabLayout) mLayout.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mSlidingTabLayout.setCustomTabView(R.layout.icon_tab, R.id.icon_tab_label, R.id.icon_tab_icon);

        mSlidingTabLayout.setViewPager(slidingPanelViewPager);

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) mLayout.findViewById(R.id.fab_add_users);
        if(isOwner()){
            fab.setSize(FloatingActionButton.SIZE_NORMAL);
            fab.setColor(getResources().getColor(R.color.primary));

            fab.initBackground();
            fab.setImageResource(R.drawable.ic_action_add_person);
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }

        return mLayout;
    }

    public HaystackVO getHaystack() {
        return haystack;
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
    }

    public boolean isOwner(){
        int userId = ((MainActivity) getActivity()).getUserModel().getUserId();
        int ownerId = haystack.getOwner();

        mIsOwner = userId == ownerId;

        return mIsOwner;
    }

    public boolean isOwner(int userId){
        int ownerId = haystack.getOwner();

        mIsOwner = userId == ownerId;

        return mIsOwner;
    }

    public class SlidingPanelPagerAdapter extends FragmentStatePagerAdapter {
        public SlidingPanelPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            switch (position){
                case 0://Pins tab
                    fragment = new DemoObjectFragment();
                    break;
                case 1://Directions tab
                    fragment = new DemoObjectFragment();
                    break;
                case 2://Users tab
                    fragment = new UsersTabFragment();
                    break;
                default:
                    fragment = new DemoObjectFragment();
                    break;
            }

            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, position + 1);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;
            switch (position){
                case 0:
                    title = getString(R.string.title_pins);
                    break;
                case 1:
                    title = getString(R.string.title_directions);
                    break;
                case 2:
                    title = getString(R.string.title_users);
                    break;
                default:
                    title = "tab " + String.valueOf(position);
                    break;
            }

            return title;
        }

        public Drawable getPageIcon(int position){
            Drawable icon;
            switch (position){
                case 0:
                    icon = getResources().getDrawable(R.drawable.ic_action_place);
                    break;
                case 1:
                    icon= getResources().getDrawable(R.drawable.ic_action_directions);
                    break;
                case 2:
                    icon= getResources().getDrawable(R.drawable.ic_action_group);
                    break;
                default:
                    icon = getResources().getDrawable(R.drawable.ic_action_directions);
                    break;
            }

            return icon;
        }
    }

    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_collection_object, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(R.id.view_pager_label)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));
            return rootView;
        }
    }

    public static class UsersTabFragment extends Fragment implements RetrieveUsersTask.RetrieveUsersResponseHandler{
        public static final String ARG_OBJECT = "object";

        private ListView listView;

        private ArrayList<UserVO> userList = new ArrayList<UserVO>();
        private ArrayList<UserVO> addedUserList = new ArrayList<UserVO>();
        private HaystackUserListAdapter userListAdapter;
        private LayoutInflater mInflater;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mInflater = inflater;
            View rootView = inflater.inflate(R.layout.fragment_users_tab, container, false);

            listView =  (ListView) rootView.findViewById(R.id.userList);
            userListAdapter = new HaystackUserListAdapter(getActivity(), R.layout.haystack_drawer_item, userList, addedUserList, inflater);
            listView.setAdapter(userListAdapter);

            fetchAllUsers();

            return rootView;
        }

        private void fetchAllUsers(){
            RetrieveUsersParams params = new RetrieveUsersParams();
            params.userId = String.valueOf(((MainActivity) getActivity()).getUserModel().getUserId());
            params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_ALL_USERS;

            try{
                RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
                task.execute();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        public void onUsersRetrieved(RetrieveUsersResult result){
            userList = result.userList;
            updateUserList();
        }

        private void updateUserList(){
            userListAdapter = new HaystackUserListAdapter(getActivity(), R.layout.haystack_drawer_item, userList, addedUserList, mInflater);
            listView.setAdapter(userListAdapter);

            userListAdapter.notifyDataSetChanged();
            listView.invalidate();
        }
    }
}