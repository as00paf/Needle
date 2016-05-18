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
import android.widget.Toast;

import com.nemator.needle.R;
import com.nemator.needle.activities.NeedleActivity;
import com.nemator.needle.controller.NeedleController;
import com.nemator.needle.fragments.needle.NeedleCardListener;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.utils.AppConstants;

public class LocationCardHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, NeedleController.CancelShareBackDelegate, NeedleController.ShareLocationBackDelegate {
    public TextView titleView, active_until, emptyText;
    public ImageView imageView, shareBackIndicator;
    public ImageButton menuButton;

    private NeedleVO needleData;

    private NeedleCardListener mListener;

    private Boolean isSent;

    private Boolean shareBack = false;

    public LocationCardHolder(View view, NeedleCardListener listener, Boolean isSent) {
        super(view);
        mListener = listener;
        this.isSent = isSent;
        titleView =  (TextView) view.findViewById(R.id.username_label);
        active_until = (TextView)  view.findViewById(R.id.time_limit_label);
        emptyText = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.image);
        menuButton = (ImageButton) view.findViewById(R.id.menu_button);
        shareBackIndicator = (ImageView) view.findViewById(R.id.share_back_indicator);
    }

    public void setData(NeedleVO locationSharing){
        needleData = locationSharing;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationSharingIntent = new Intent(itemView.getContext(), NeedleActivity.class);
                locationSharingIntent.putExtra(AppConstants.TAG_LOCATION_SHARING, (Parcelable) needleData);
                itemView.getContext().startActivity(locationSharingIntent);
            }
        });

        menuButton.setOnClickListener(this);
        setShareBack(locationSharing.isSharedBack());
    }

    public NeedleVO getNeedleData() {
        return needleData;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        final LocationCardHolder me = this;
        switch (item.getItemId()) {
            case R.id.menu_option_share_back:
                if(needleData.isSharedBack()){
                    NeedleController.cancelShareBack(needleData, me);
                }else{
                    NeedleController.shareLocationBack(needleData, me);
                }
                break;
            case R.id.menu_option_cancel_haystack:
                new AlertDialog.Builder(itemView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(itemView.getContext().getString(R.string.cancel))
                        .setMessage(itemView.getContext().getString(R.string.cancel_needle_confirmation))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onCancelLocationSharing(me.needleData);
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

        int resourceId = isSent ? R.menu.menu_needle_sent_card : R.menu.menu_needle_received_card;
        inflater.inflate(resourceId, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        if(!isSent){
            popup.getMenu().getItem(0).setTitle(shareBack ? itemView.getContext().getString(R.string.stop_sharing_location) :
                    itemView.getContext().getString(R.string.share_location_back));
        }

        popup.show();
    }

    public void setShareBack(Boolean value) {
        shareBack = value;
        shareBackIndicator.setVisibility(shareBack ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onShareBackCancelSuccess(NeedleVO locationSharing) {
        setData(locationSharing);
    }

    @Override
    public void onShareBackCancelFailed(String result) {
        Toast.makeText(itemView.getContext(), result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationShareBackSuccess(NeedleVO locationSharing) {
        setData(locationSharing);
    }

    @Override
    public void onLocationShareBackFailed(String result) {
        Toast.makeText(itemView.getContext(), result, Toast.LENGTH_SHORT).show();
    }
}

