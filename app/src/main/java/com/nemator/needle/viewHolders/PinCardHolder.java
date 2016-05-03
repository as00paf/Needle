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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.activities.LocationSharingActivity;
import com.nemator.needle.adapter.LocationSharingCardAdapter;
import com.nemator.needle.adapter.PinCardAdapter;
import com.nemator.needle.fragments.locationSharing.LocationSharingCardListener;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.utils.AppConstants;

public class PinCardHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    public ImageButton menuButton;
    public TextView titleView;

    private PinVO data;
    private PinCardAdapter.PinCardListener listener;

    public PinCardHolder(View view, PinCardAdapter.PinCardListener listener) {
        super(view);
        this.listener = listener;

        titleView =  (TextView) view.findViewById(R.id.title);
        menuButton = (ImageButton) view.findViewById(R.id.menu_button);
    }

    public void setData(PinVO data) {
        this.data = data;

        titleView.setText(data.getText());

        menuButton.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return listener.onMenuItemClick(item);
    }

    @Override
    public void onClick(View v) {
        if(v == titleView){
            listener.onClick(data);
        }else if(v  == menuButton){
            PopupMenu popup = new PopupMenu(v.getContext(), menuButton);
            MenuInflater inflater = popup.getMenuInflater();

            inflater.inflate(R.menu.menu_haystack_card, popup.getMenu());
            popup.setOnMenuItemClickListener(this);

            popup.show();
        }
    }
}

