package com.nemator.needle.viewHolders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.adapter.LocationSharingListCardAdapter;
import com.nemator.needle.fragments.locationSharing.LocationSharingCardListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;

public class LocationSharingCardHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener
{
    public TextView titleView, active_until, emptyText;
    public ImageView imageView, shareBackIndicator;
    public ImageButton menuButton;

    private LocationSharingVO locationSharingData;

    private LocationSharingCardListener mListener;

    private Boolean isSent;
    private LocationSharingListCardAdapter adapter;

    private Boolean shareBack = false;

    public LocationSharingCardHolder(LocationSharingListCardAdapter adapter, View view, LocationSharingCardListener listener, Boolean isSent) {
        super(view);
        mListener = listener;
        this.isSent = isSent;
        titleView =  (TextView) view.findViewById(R.id.username_label);
        active_until = (TextView)  view.findViewById(R.id.time_limit_label);
        emptyText = (TextView) view.findViewById(R.id.emptyText);
        imageView = (ImageView) view.findViewById(R.id.location_sharing_card_image);
        menuButton = (ImageButton) view.findViewById(R.id.location_sharing_card_menu_button);
        shareBackIndicator = (ImageView) view.findViewById(R.id.location_sharing_share_back_indicator);
        this.adapter = adapter;
    }

    public LocationSharingCardHolder(View view) {
        super(view);
        titleView =  (TextView) view.findViewById(R.id.username_label);
        active_until = (TextView)  view.findViewById(R.id.time_limit_label);
        emptyText = (TextView) view.findViewById(R.id.emptyText);
        imageView = (ImageView) view.findViewById(R.id.location_sharing_card_image);
    }

    public void setData(LocationSharingVO locationSharing){
        locationSharingData = locationSharing;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationSharingIntent = new Intent(itemView.getContext(), LocationSharingActivity.class);
                locationSharingIntent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Parcelable) locationSharingData);
                itemView.getContext().startActivity(locationSharingIntent);
            }
        });

        menuButton.setOnClickListener(this);
        setShareBack(locationSharing.isSharedBack());
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        final LocationSharingCardHolder me = this;
        switch (item.getItemId()) {
            case R.id.menu_option_share_back:
                adapter.shareLocationBack(item, me);
                break;
            case R.id.menu_option_cancel:
                new AlertDialog.Builder(itemView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(itemView.getContext().getString(R.string.cancel))
                        .setMessage(itemView.getContext().getString(R.string.cancel_location_sharing_confirmation))
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
            popup.getMenu().getItem(0).setTitle(shareBack ? itemView.getContext().getString(R.string.stop_sharing_location) :
                    itemView.getContext().getString(R.string.share_location_back));
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

