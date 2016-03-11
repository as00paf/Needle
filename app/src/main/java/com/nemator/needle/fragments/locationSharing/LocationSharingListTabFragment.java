package com.nemator.needle.fragments.locationSharing;

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
import com.nemator.needle.adapter.LocationSharingListCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppState;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSharingListTabFragment extends Fragment implements LocationSharingCardListener{
    private static final String TAG = "ListTabFragment";

    //Views
    private View rootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    //Data
    private ArrayList<LocationSharingVO> dataList;
    private Boolean isReceived;

    //Objects
    private LocationSharingListCardAdapter listAdapter;
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
                    + " must implement OnActivityStateChangeListener and LocationSharingCardListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_sharing_tab, container, false);

        //Recycler View
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.location_sharing_list);
        mRecyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return listAdapter.getItemViewType(position) == 0 ? 1 : 2;
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);

        listAdapter = new LocationSharingListCardAdapter(dataList, getActivity(), !isReceived, this);
        mRecyclerView.setAdapter(listAdapter);


        //Swipe To Refresh
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshList();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        int state = isReceived ? AppState.LOCATION_SHARING_RECEIVED_TAB : AppState.LOCATION_SHARING_SENT_TAB;
        if(stateChangeCallback!=null) stateChangeCallback.onStateChange(state);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(listAdapter != null){
            mRecyclerView.setAdapter(listAdapter);
        }
    }

    public void updateLocationSharingList(ArrayList<LocationSharingVO> data){
        this.dataList = data;
        listAdapter = new LocationSharingListCardAdapter(dataList, getActivity(), !isReceived, this);

        if(mRecyclerView != null){
            mRecyclerView.setAdapter(listAdapter);
        }
    }

    //Getters/Setters
    public SwipeRefreshLayout getRefreshLayout() {
        return swipeLayout;
    }

    //Handlers
    @Override
    public void onRefreshList() {
        Needle.navigationController.refreshLocationSharingList();
    }

    @Override
    public void onCancelLocationSharing(LocationSharingVO vo) {
        ApiClient.getInstance().cancelLocationSharing(vo, cancelLocationSharingCallback);
    }

    private Callback<LocationSharingResult> cancelLocationSharingCallback = new Callback<LocationSharingResult>() {

        @Override
        public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
            LocationSharingResult result = response.body();
            if(result.getSuccessCode() == 1){
                //Remove from list
                int index = -1;

                for (LocationSharingVO vo : listAdapter.getListData()){
                    if(vo.getId() == result.getLocationSharing().getId()){
                        index = listAdapter.getListData().indexOf(vo);
                    }
                }

                if(index > -1){
                    listAdapter.getListData().remove(index);
                    listAdapter.notifyDataSetChanged();
                }

                //TODO : Localize
                Toast.makeText(getContext(), "Location Sharing was cancelled", Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not cancel location sharing! Error : " + result.getMessage());
                Toast.makeText(getContext(), "Could not cancel location sharing!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<LocationSharingResult> call, Throwable t) {
            Log.e(TAG, "Could not cancel location sharing! Error : " + t.getMessage());
            Toast.makeText(getContext(), "Could not cancel location sharing!", Toast.LENGTH_SHORT).show();
        }
    };
}
