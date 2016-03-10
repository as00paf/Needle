package com.nemator.needle.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.fragments.locationSharing.LocationSharingCardListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSharingListCardAdapter extends RecyclerView.Adapter<LocationSharingListCardAdapter.LocationSharingCardViewHolder>{
    public static String TAG = "LocationSharingListCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;
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
    public LocationSharingCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocationSharingCardViewHolder viewHolder;
        View locationSharingCard;

        if(viewType == TYPE_ITEM){
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_location_sharing, parent, false);
            viewHolder = new LocationSharingCardViewHolder(this, locationSharingCard, true, mListener, isSent);
        }else{
            locationSharingCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new LocationSharingCardViewHolder(this, locationSharingCard, false, mListener, isSent);
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

    //TODO : move from here ?
    public void shareLocationBack(MenuItem item, LocationSharingCardViewHolder viewHolder){
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

    //TODO : own class
    public static class LocationSharingCardViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener
    {
        //Item
        private TextView titleView, active_until;
        private ImageView imageView, shareBackIndicator;
        private ImageButton menuButton;

        //Empty
        private TextView emptyText;

        private LocationSharingVO locationSharingData;

        private LocationSharingCardListener mListener;

        private Boolean isSent;
        private LocationSharingListCardAdapter adapter;

        private Boolean shareBack = false;

        public LocationSharingCardViewHolder(LocationSharingListCardAdapter adapter, View view, Boolean isNotEmpty, LocationSharingCardListener listener, Boolean isSent) {
            super(view);
            mListener = listener;
            this.isSent = isSent;
            titleView =  (TextView) view.findViewById(R.id.location_sharing_name_label);
            active_until = (TextView)  view.findViewById(R.id.location_sharing_time_limit_label);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
            menuButton = (ImageButton) view.findViewById(R.id.location_sharing_card_menu_button);
            shareBackIndicator = (ImageView) view.findViewById(R.id.location_sharing_share_back_indicator);
            this.adapter = adapter;
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
                    Intent locationSharingIntent = new Intent(mContext, LocationSharingActivity.class);
                    locationSharingIntent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Parcelable) locationSharingData);
                    mContext.startActivity(locationSharingIntent);
                }
            });

            menuButton.setOnClickListener(this);
            setShareBack(locationSharing.getShareBack());
        }

        @Override
        public boolean onMenuItemClick(final MenuItem item) {
            final LocationSharingCardViewHolder me = this;
            switch (item.getItemId()) {
                 case R.id.menu_option_share_back:
                     adapter.shareLocationBack(item, me);
                     break;
                case R.id.menu_option_cancel:
                    new AlertDialog.Builder(mContext)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(mContext.getString(R.string.cancel))
                            .setMessage(mContext.getString(R.string.cancel_location_sharing_confirmation))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mListener.onCancelLocationSharing(me.locationSharingData);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                    break;
                default:
                    return false;
            }

            return true;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(v.getContext(), menuButton);
            MenuInflater inflater = popup.getMenuInflater();

            int resourceId = isSent ? R.menu.menu_location_sharing_sent_card : R.menu.menu_location_sharing_received_card;
            inflater.inflate(resourceId, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            if(!isSent){
                popup.getMenu().getItem(0).setTitle(shareBack ? mContext.getString(R.string.stop_sharing_location) :
                                                                mContext.getString(R.string.share_location_back));
            }

            popup.show();
        }

        public Boolean getIsSent() {
            return isSent;
        }

        public void setShareBack(Boolean value) {
            shareBack = value;
            shareBackIndicator.setVisibility(shareBack ? View.VISIBLE : View.INVISIBLE);
        }

        public Boolean getShareBack() {
            return shareBack;
        }

        public Boolean toggleShareBack(){
            setShareBack(!shareBack);
            return shareBack;
        }
    }

    //Getters/Setters
    public ArrayList<LocationSharingVO> getListData() {
        return listData;
    }

    public void setListData(ArrayList<LocationSharingVO> listData) {
        this.listData = listData;
    }
}

