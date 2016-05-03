package com.nemator.needle.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.fragments.locationSharing.LocationSharingCardListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.viewHolders.LocationSharingCardHolder;
import com.nemator.needle.viewHolders.PinCardHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinCardAdapter extends RecyclerView.Adapter<PinCardHolder>{
    public static String TAG = "PinCardAdapter";

    private ArrayList<PinVO> listData;
    private static Context mContext;

    private PinCardListener mListener;

    public PinCardAdapter(ArrayList<PinVO> data, Context context, PinCardListener listener) {
        mListener = listener;
        listData = data;
        mContext = context;
        if(listData == null){
            listData = new ArrayList<PinVO>();
        }
    }

    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public PinCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PinCardHolder viewHolder;
        View pinCard;

        pinCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pin, parent, false);
        viewHolder = new PinCardHolder(pinCard, mListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PinCardHolder holder, int position) {
        holder.setData((PinVO) getItem(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public interface PinCardListener {
        void onClick(PinVO pinVO);
        boolean onMenuItemClick(MenuItem item);
    }

    //Getters/Setters
    public ArrayList<PinVO> getListData() {
        return listData;
    }

    public void setListData(ArrayList<PinVO> listData) {
        this.listData = listData;
    }
}

