package com.nemator.needle.view.locationSharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.ImageDownloaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.view.haystack.HaystackActivity;

import java.util.ArrayList;

public class LocationSharingListCardAdapter extends RecyclerView.Adapter<LocationSharingListCardAdapter.LocationSharingCardViewHolder> {
    public static String TAG = "LocationSharingListCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;

    private ArrayList<LocationSharingVO> listData;
    private Context mContext;

    public LocationSharingListCardAdapter(ArrayList<LocationSharingVO> data, Context context) {
        listData = data;
        mContext = context;

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
            viewHolder = new LocationSharingCardViewHolder(locationSharingCard, true);
        }else{
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new LocationSharingCardViewHolder(locationSharingCard, false);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LocationSharingCardViewHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                LocationSharingVO locationSharing = (LocationSharingVO) listData.get(position);
                holder.titleView.setText(locationSharing.senderName);

                String activeUntil = locationSharing.timeLimit;
                activeUntil = activeUntil.replace("00:00:00", "");
                activeUntil = activeUntil.replace(":00", "");
                holder.active_until.setText(activeUntil);

                if (holder.imageView != null && locationSharing.pictureURL != null) {
                    String encodedURL = AppConstants.HAYSTACK_PICTURES_URL + Uri.encode(locationSharing.pictureURL);
                    new ImageDownloaderTask(holder.imageView).execute(encodedURL);
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
        if(getItem(position) instanceof HaystackVO){
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

        public LocationSharingCardViewHolder(View view, Boolean isNotEmpty) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.title);
            active_until = (TextView)  view.findViewById(R.id.active_until);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }

        public LocationSharingCardViewHolder(View view) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.title);
            active_until = (TextView)  view.findViewById(R.id.active_until);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }

        public void setData(LocationSharingVO locationSharing){
            locationSharingData = locationSharing;
           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), LocationSharing.class);
                    view.getContext().startActivity(intent);
                }
            });*/
        }
    }
}

