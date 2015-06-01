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

import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;

public class HaystackListTabFragment extends Fragment{
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

        HaystackListFragment parentFragment = (HaystackListFragment) getFragmentManager().getFragments().get(0);
        dataList = isPublic ? parentFragment.publicHaystacks : parentFragment.privateHaystacks;
        mAdapter = new HaystackListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener((HaystackListFragment) getParentFragment());

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        int state = isPublic ? AppState.PUBLIC_HAYSTACK_TAB : AppState.PRIVATE_HAYSTACK_TAB;
        stateChangeCallback.onStateChange(state);
    }

    public void updateHaystackList(){
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }
}
