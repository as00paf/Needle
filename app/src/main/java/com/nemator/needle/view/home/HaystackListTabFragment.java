package com.nemator.needle.view.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nemator.needle.R;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksParams;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksResult;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksTask;
import com.nemator.needle.models.vo.HaystackVO;

import java.util.ArrayList;

public class HaystackListTabFragment extends Fragment implements FetchHaystacksTask.FetchHaystackResponseHandler, SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "HaystackListTabFragment";

    private ProgressBar progressbar = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<HaystackVO> dataList;
    private View rootView;
    private SwipeRefreshLayout swipeLayout;

    private ArrayList<HaystackVO> publicHaystacks = null;
    private ArrayList<HaystackVO> privateHaystacks = null;

    private String userName;
    private int userId = -1;
    private Boolean isPublic;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            isPublic = args.getBoolean("isPublic", true);
        }

        if(savedInstanceState != null){
            if(isPublic){
                publicHaystacks = savedInstanceState.getParcelableArrayList("publicHaystacks");
            }else{
                privateHaystacks = savedInstanceState.getParcelableArrayList("privateHaystacks");
            }
        }else {
            updateHaystackList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(isPublic){
            outState.putParcelableArrayList("publicHaystacks", publicHaystacks);
        }else{
            outState.putParcelableArrayList("privateHaystacks", privateHaystacks);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_haystack_list_tab, container, false);

        //Progress bar
        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.haystack_list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HaystackListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        return rootView;
    }

    public void updateDataList(ArrayList<HaystackVO> data){
        dataList = data;
        mAdapter = new HaystackListCardAdapter(dataList, rootView.getContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchHaystacks();
    }

    public void fetchHaystacks(){
        FetchHaystacksParams params = new FetchHaystacksParams(getUserName(), String.valueOf(getUserId()), rootView.getContext(), progressbar);

        try{
            FetchHaystacksTask task = new FetchHaystacksTask(params, this);
            task.execute();
        }catch(Exception e){
            Log.e(TAG, "fetchHaystacks exception : " + e.toString());
        }
    }

    @Override public void onRefresh() {
        fetchHaystacks();
    }

    public void onHaystackFetched(FetchHaystacksResult result){
        publicHaystacks = result.publicHaystackList;
        privateHaystacks = result.privateHaystackList;

        updateHaystackList();
    }

    public void updateHaystackList() {
        if(rootView == null){
            return;
        }

        if(isPublic){
            updateDataList(publicHaystacks);
        }else{
            updateDataList(privateHaystacks);
        }

        progressbar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        swipeLayout.setRefreshing(false);
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    private String getUserName(){
        if(userName == null){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userName = sp.getString("username", null);
        }

        return userName;
    }
}
