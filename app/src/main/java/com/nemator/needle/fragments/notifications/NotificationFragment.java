package com.nemator.needle.fragments.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.NotificationCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.NotificationResult;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.viewHolders.NotificationCardHolder;

import java.util.ArrayList;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment  extends Fragment {
    private static final String TAG = "NotificationFragment";

    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    private LinearLayoutManager mLayoutManager;
    private NotificationCardAdapter mAdapter;
    private ArrayList<NotificationVO> dataList;

    public NotificationFragment() {
    }

    public static NotificationFragment newInstance(){
        return new NotificationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_tab, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NotificationCardAdapter(this.dataList, getActivity(), cardListener);
        mRecyclerView.setAdapter(mAdapter);

        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNotifications();
            }
        });

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                fetchNotifications();
                swipeLayout.setRefreshing(true);
            }
        }, 100);

        return rootView;
    }

    public void fetchNotifications() {
        Log.d(TAG, "Fetching Notifications !");
        ApiClient.getInstance().fetchNotifications(Needle.userModel.getUserId(), fetchNotificationsCallback);
    }

    private Callback<NotificationResult> fetchNotificationsCallback = new Callback<NotificationResult>() {
        @Override
        public void onResponse(Call<NotificationResult> call, Response<NotificationResult> response) {
            swipeLayout.setRefreshing(false);
            NotificationResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Fetch notifications. Success : " + result.getMessage());

                updateNotficationList(result.getNotifications());
            }else{
                Toast.makeText(getContext(), "Could not fetch notifications", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Could not fetch notifications. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<NotificationResult> call, Throwable t) {
            if(getActivity() != null && swipeLayout != null){
                Toast.makeText(getActivity(), "Could not fetch notifications", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Could not fetch notifications. Error : " + t.getMessage());
                swipeLayout.setRefreshing(false);
            }
        }
    };


    private NotificationCardHolder.NotificationCardListener cardListener = new NotificationCardHolder.NotificationCardListener() {
        @Override
        public void onClick(View view, NotificationVO vo) {
            Log.d(TAG, "click");
        }

        @Override
        public void onClickUser(NotificationVO vo) {
            Log.d(TAG, "click user");
        }
    };

    public void updateNotficationList(ArrayList<NotificationVO> data){
        this.dataList = data;

        mAdapter = new NotificationCardAdapter(this.dataList, getActivity(), cardListener);

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }

        Iterator<NotificationVO> notifications = data.iterator();
        ArrayList<NotificationVO> unseenNotifications = new ArrayList<NotificationVO>();
        while(notifications.hasNext()){
            NotificationVO notif = notifications.next();
            if(!notif.getSeen()){
                unseenNotifications.add(notif);
            }
        }

        if(unseenNotifications.size() > 0){
            ApiClient.getInstance().seenNotifications(unseenNotifications, new Callback<NotificationResult>() {
                @Override
                public void onResponse(Call<NotificationResult> call, Response<NotificationResult> response) {
                    NotificationResult result = response.body();
                    if(result.getSuccessCode() == 1){
                        Log.d(TAG, "Notifications successfuly updated");
                    }else{
                        Log.d(TAG, "Notifications failed to update. Error : " + result.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<NotificationResult> call, Throwable t) {
                    Log.d(TAG, "Notifications failed to update. Error : " + t.getMessage());
                }
            });
        }
    }
}
