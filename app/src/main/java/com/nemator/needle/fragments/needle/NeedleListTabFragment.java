package com.nemator.needle.fragments.needle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.NeedleCardAdapter;
import com.nemator.needle.controller.NeedleController;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.NeedleVO;

import java.util.ArrayList;

public class NeedleListTabFragment extends Fragment implements NeedleCardListener, NeedleController.CancelNeedleDelegate {
    private static final String TAG = "ListTabFragment";

    //Views
    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<NeedleVO> dataList;
    private Boolean isReceived;

    //Objects
    private NeedleCardAdapter listAdapter;
    private GridLayoutManager layoutManager;
    private OnActivityStateChangeListener stateChangeCallback;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null){
            isReceived = args.getBoolean("isReceived", true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = Needle.navigationController;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener and NeedleCardListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_needle_list_tab, container, false);

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

            listAdapter = new NeedleCardAdapter(dataList, getActivity(), !isReceived, this);
            mRecyclerView.setAdapter(listAdapter);

            //Swipe To Refresh
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshList();
                }
            });
            swipeLayout.setRefreshing(true);
        }


        return rootView;
    }

    public void updateNeedlesList(ArrayList<NeedleVO> data){
        this.dataList = data;
        listAdapter = new NeedleCardAdapter(dataList, getActivity(), !isReceived, this);

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
    @Override
    public void onRefreshList() {
        Needle.navigationController.refreshNeedleList();
    }

    @Override
    public void onCancelLocationSharing(NeedleVO vo) {
        NeedleController.cancelNeedle(vo, this);
    }

    @Override
    public void onNeedleCancelSuccess(NeedleVO locationSharing) {
        //Remove from list
        int index = -1;

        for (NeedleVO vo : listAdapter.getListData()){
            if(vo.getId() == locationSharing.getId()){
                index = listAdapter.getListData().indexOf(vo);
            }
        }

        if(index > -1){
            listAdapter.getListData().remove(index);
            listAdapter.notifyDataSetChanged();
        }

        //TODO : Localize
        Toast.makeText(getContext(), "Location Sharing was cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNeedleCancelFailed(String result) {
        Log.e(TAG, result);
        Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
    }
}
