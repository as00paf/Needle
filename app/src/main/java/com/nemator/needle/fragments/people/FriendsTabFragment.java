package com.nemator.needle.fragments.people;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class FriendsTabFragment extends Fragment{
    private static final String TAG = "ListTabFragment";

    //Views
    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<UserVO> dataList;
    private int type;

    //Objects
    private UserCardAdapter listAdapter;
    private GridLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            type = args.getInt("type", -1);//TODO : use constant
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_friends_list_tab, container, false);

            //Recycler View
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);

            layoutManager = new GridLayoutManager(getActivity(), 2);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return listAdapter.getItemViewType(position) == 0 ? 1 : 2;
                }
            });
            mRecyclerView.setLayoutManager(layoutManager);

            listAdapter = new UserCardAdapter(getActivity(), dataList, UserCardAdapter.Type.SHOW_PROFILE);
            mRecyclerView.setAdapter(listAdapter);

            //Swipe To Refresh
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshList();
                }
            });

        }

        return rootView;
    }

    public void updateFriendsList(ArrayList<UserVO> data){
        this.dataList = data;
        listAdapter = new UserCardAdapter(getActivity(), dataList, UserCardAdapter.Type.SHOW_PROFILE);

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }

    public void setRefreshing(boolean value){
        if(swipeLayout!=null) swipeLayout.setRefreshing(value);
    }

    //Handlers
    public void onRefreshList() {
        Needle.navigationController.refreshFriendsList();
    }
}
