package com.nemator.needle.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask;

import java.util.ArrayList;

public class SlidingPanelPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;

    public SlidingPanelPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
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
                title = context.getString(R.string.title_pins);
                break;
            case 1:
                title = context.getString(R.string.title_directions);
                break;
            case 2:
                title = context.getString(R.string.title_users);
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
                icon = context.getResources().getDrawable(R.drawable.ic_action_place);
                break;
            case 1:
                icon= context.getResources().getDrawable(R.drawable.ic_action_directions);
                break;
            case 2:
                icon= context.getResources().getDrawable(R.drawable.ic_action_group);
                break;
            default:
                icon = context.getResources().getDrawable(R.drawable.ic_action_directions);
                break;
        }

        return icon;
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
            params.userId = String.valueOf(Needle.userModel.getUserId());
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