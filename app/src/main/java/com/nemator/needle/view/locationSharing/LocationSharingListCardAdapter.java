package com.nemator.needle.view.locationSharing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.view.locationSharing.LocationSharingListFragment.LocationSharingListFragmentInteractionListener;

import java.util.ArrayList;

public class LocationSharingListCardAdapter extends RecyclerView.Adapter<LocationSharingListCardAdapter.LocationSharingCardViewHolder> {
    public static String TAG = "LocationSharingListCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;
    private Boolean isSent;

    private ArrayList<LocationSharingVO> listData;
    private Context mContext;

    private LocationSharingListFragmentInteractionListener mListener;

    public LocationSharingListCardAdapter(ArrayList<LocationSharingVO> data, Context context, Boolean isSent, LocationSharingListFragmentInteractionListener listener) {
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
    public LocationSharingCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationSharingCardViewHolder viewHolder;
        View locationSharingCard;

        if(viewType == TYPE_ITEM){
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_sharing_card_layout, parent, false);
            viewHolder = new LocationSharingCardViewHolder(locationSharingCard, true, mListener, isSent);
        }else{
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new LocationSharingCardViewHolder(locationSharingCard, false, mListener, isSent);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LocationSharingCardViewHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                LocationSharingVO locationSharing = (LocationSharingVO) listData.get(position);
                holder.titleView.setText(isSent ? locationSharing.getReceiverName() :
                                                    locationSharing.getSenderName());

                String activeUntil = locationSharing.getTimeLimit();
                activeUntil = activeUntil.replace("00:00:00", "");
                activeUntil = activeUntil.replace(":00", "");
                holder.active_until.setText(activeUntil);

                if (holder.imageView != null) {
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

    public static class LocationSharingCardViewHolder extends RecyclerView.ViewHolder{
        //Item
        TextView titleView, active_until;
        ImageView imageView;

        //Empty
        TextView emptyText;

        LocationSharingVO locationSharingData;

        private LocationSharingListFragmentInteractionListener mListener;
        private Boolean isSent;

        public LocationSharingCardViewHolder(View view, Boolean isNotEmpty, LocationSharingListFragmentInteractionListener listener, Boolean isSent) {
            super(view);
            mListener = listener;
            this.isSent = isSent;
            titleView =  (TextView) view.findViewById(R.id.location_sharing_name_label);
            active_until = (TextView)  view.findViewById(R.id.location_sharing_time_limit_label);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }

        public LocationSharingCardViewHolder(View view) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.location_sharing_name_label);
            active_until = (TextView)  view.findViewById(R.id.location_sharing_time_limit_label);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }

        public void setData(LocationSharingVO locationSharing){
            locationSharingData = locationSharing;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClickLocationSharingCard(locationSharingData, isSent);
                }
            });
        }
    }
}

