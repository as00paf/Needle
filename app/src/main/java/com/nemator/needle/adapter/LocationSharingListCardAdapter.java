package com.nemator.needle.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.nemator.needle.utils.CropCircleTransformation;
import com.nemator.needle.viewHolders.LocationSharingCardHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSharingListCardAdapter extends RecyclerView.Adapter<LocationSharingCardHolder>{
    public static String TAG = "LocationSharingListCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 1;
    private Boolean isSent;

    private ArrayList<LocationSharingVO> listData;
    private static Context mContext;

    private LocationSharingCardListener mListener;

    public LocationSharingListCardAdapter(ArrayList<LocationSharingVO> data, Context context, Boolean isSent, LocationSharingCardListener listener) {
        mListener = listener;
        listData = data;
        mContext = context;
        this.isSent = isSent;
        if(listData == null){
            listData = new ArrayList<LocationSharingVO>();
        }
    }

    public Object getItem(int position) {
        if(listData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public LocationSharingCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationSharingCardHolder viewHolder;
        View locationSharingCard;

        if(viewType == TYPE_ITEM){
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_location_sharing, parent, false);
            viewHolder = new LocationSharingCardHolder(this, locationSharingCard, mListener, isSent);
        }else{
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new LocationSharingCardHolder(this, locationSharingCard, mListener, isSent);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LocationSharingCardHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                LocationSharingVO locationSharing = (LocationSharingVO) listData.get(position);
                holder.titleView.setText(isSent ? locationSharing.getReceiver().getReadableUserName() :
                                                    locationSharing.getSender().getReadableUserName());

                String activeUntil = locationSharing.getTimeLimit();
                activeUntil = activeUntil.replace("00:00:00", "");
                activeUntil = activeUntil.replace(":00", "");
                holder.active_until.setText(activeUntil);

                if (holder.imageView != null) {

                    int padding = 8 * 2;
                    DisplayMetrics dm = new DisplayMetrics();
                    ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = dm.widthPixels / 2 - padding;

                    holder.itemView.getLayoutParams().height  = width;

                    //Image
                    if(locationSharing.getSender().getPictureURL() != null && !locationSharing.getSender().getPictureURL().isEmpty()){
                        Picasso.with(mContext)
                                .load(locationSharing.getSender().getPictureURL())
                                .resize(width,width)
                                .into(holder.imageView);
                    }else{
                        Log.d(TAG, "Cant get picture URL for user " + locationSharing.getSender().getUserName());
                    }

                    holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person_placeholder));
                }

                holder.setData(locationSharing);

                break;
            case TYPE_EMPTY:
                holder.emptyText.setText(mContext.getResources().getString(R.string.noLocationSharingAvailable));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(listData.size() == 0)
            return 1;
        return listData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) instanceof LocationSharingVO){
            return TYPE_ITEM;
        }else{
            return TYPE_EMPTY;
        }
    }

    //TODO : move from here ?
    public void shareLocationBack(MenuItem item, LocationSharingCardHolder viewHolder){
        LocationSharingVO vo = ((LocationSharingVO) getItem(viewHolder.getPosition())).clone();
        ApiClient.getInstance().shareLocationBack(vo, shareLocationBackCallback);
    }

    private Callback<LocationSharingResult> shareLocationBackCallback = new Callback<LocationSharingResult>() {
        @Override
        public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
            //TODO : show share back icon on card
            LocationSharingResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Location shared back !");
            }else{
                Log.d(TAG, "Failed to share location back !");
            }

            //LocationSharingCardViewHolder viewHolder = result.viewHolderRef.get();
            //viewHolder.setShareBack(result.getLocationSharing().getShareBack());

            //mListener.onLocationSharingUpdated(result);
        }

        @Override
        public void onFailure(Call<LocationSharingResult> call, Throwable t) {
            Log.d(TAG, "Failed to share location back !");
        }
    };

    //Getters/Setters
    public ArrayList<LocationSharingVO> getListData() {
        return listData;
    }

    public void setListData(ArrayList<LocationSharingVO> listData) {
        this.listData = listData;
    }
}

