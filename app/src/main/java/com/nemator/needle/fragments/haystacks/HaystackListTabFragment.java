package com.nemator.needle.fragments.haystacks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.adapter.HaystackCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackListTabFragment extends Fragment {
    private static final String TAG = "HaystackListTabFragment";

    //Views
    private View rootView;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView mRecyclerView;

    //Objects
    private OnActivityStateChangeListener stateChangeCallback;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Data
    private ArrayList<HaystackVO> dataList;
    private Boolean isPublic;
    private HaystackListFragmentInteractionListener listener;

    public static HaystackListTabFragment newInstance(HaystackListTabFragment.HaystackListFragmentInteractionListener listener){
        HaystackListTabFragment fragment = new HaystackListTabFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            isPublic = args.getBoolean("isPublic", true);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_tab, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HaystackCardAdapter(this.dataList, getActivity(), (HaystackListTabFragment.HaystackListFragmentInteractionListener) getParentFragment());
        mRecyclerView.setAdapter(mAdapter);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onRefreshHaystackList();
            }
        });
        swipeLayout.setRefreshing(true);

        return rootView;
    }

    public void updateHaystackList(ArrayList<HaystackVO> data){
        this.dataList = data;

        mAdapter = new HaystackCardAdapter(this.dataList, getActivity(), listener);

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }

    public static interface HaystackListFragmentInteractionListener {
        void onRefreshHaystackList();
        void onClickHaystackCard(HaystackVO haystack);
        void onCancelHaystack(HaystackVO haystack);
        void onLeaveHaystack(HaystackVO haystack);
    }

}
