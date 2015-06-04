package com.nemator.needle.view.haystacks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;

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
    private HaystackListFragment.HaystackListFragmentInteractionListener interactionListener;

    //Data
    private ArrayList<HaystackVO> dataList;
    private Boolean isPublic;

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
            stateChangeCallback = (OnActivityStateChangeListener) getActivity();
            interactionListener = (HaystackListFragment.HaystackListFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_haystack_list_tab, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.haystack_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                interactionListener.onRefreshHaystackList();
            }
        });

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        int state = isPublic ? AppState.PUBLIC_HAYSTACK_TAB : AppState.PRIVATE_HAYSTACK_TAB;
        stateChangeCallback.onStateChange(state);
    }

    public void updateHaystackList(ArrayList<HaystackVO> data){
        this.dataList = data;

        if(mAdapter == null){
            mAdapter = new HaystackListCardAdapter(this.dataList, getActivity());
        }else{
            mAdapter.notifyDataSetChanged();
        }

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }


}
