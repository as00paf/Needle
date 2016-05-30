package com.nemator.needle.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.fragments.needle.NeedleCardListener;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.utils.AppUtils;
import com.nemator.needle.viewHolders.LocationCardHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NeedleCardAdapter extends RecyclerView.Adapter<LocationCardHolder>{
    public static String TAG = "NeedleCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 1;
    private Boolean isSent;

    private ArrayList<NeedleVO> listData;
    private static Context mContext;

    private NeedleCardListener mListener;

    public NeedleCardAdapter(ArrayList<NeedleVO> data, Context context, Boolean isSent, NeedleCardListener listener) {
        mListener = listener;
        listData = data;
        mContext = context;
        this.isSent = isSent;
        if(listData == null){
            listData = new ArrayList<NeedleVO>();
        }
    }

    public Object getItem(int position) {
        if(listData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public LocationCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationCardHolder viewHolder;
        View locationSharingCard;

        if(viewType == TYPE_ITEM){
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_needle, parent, false);
            viewHolder = new LocationCardHolder(locationSharingCard, mListener, isSent);
        }else{
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_empty_title_view, parent, false);
            viewHolder = new LocationCardHolder(locationSharingCard, mListener, isSent);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LocationCardHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                NeedleVO locationSharing = (NeedleVO) listData.get(position);
                holder.titleView.setText(isSent ? locationSharing.getReceiver().getReadableUserName() :
                                                    locationSharing.getSender().getReadableUserName());

                holder.active_until.setText(AppUtils.formatDateUntil(mContext, locationSharing.getTimeLimit()));

                if (holder.imageView != null) {

                    int padding = 8 * 2;
                    DisplayMetrics dm = new DisplayMetrics();
                    ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = dm.widthPixels / 2 - padding;

                    holder.imageView.getLayoutParams().height  = width;

                    //Image
                    String pictureURL = isSent ? locationSharing.getReceiver().getPictureURL() : locationSharing.getSender().getPictureURL();
                    if(pictureURL != null && !pictureURL.isEmpty()){
                        Picasso.with(holder.imageView.getContext()).cancelRequest(holder.imageView);

                        Picasso.with(holder.imageView.getContext())
                                .load(pictureURL)
                                .resize(width,width)
                                .into(holder.imageView);
                    }else{
                        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person_placeholder));
                        Log.d(TAG, "Cant get picture URL for user " + locationSharing.getReceiver().getUserName());
                    }
                }

                holder.setData(locationSharing);

                break;
            case TYPE_EMPTY:
                holder.emptyText.setText(mContext.getResources().getString(R.string.noNeedleAvailable));
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
        if(getItem(position) instanceof NeedleVO){
            return TYPE_ITEM;
        }else{
            return TYPE_EMPTY;
        }
    }

    //Getters/Setters
    public ArrayList<NeedleVO> getListData() {
        return listData;
    }

    public void setListData(ArrayList<NeedleVO> listData) {
        this.listData = listData;
    }
}

