package com.nemator.needle.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.LocationSharingCardHolder;
import com.nemator.needle.viewHolders.NotificationCardHolder;

import java.util.ArrayList;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardHolder> {
    public static String TAG = "NotificationCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;
    private NotificationCardHolder.NotificationCardListener listener;

    private ArrayList<NotificationVO> listData;
    private Context mContext;

    public NotificationCardAdapter(ArrayList<NotificationVO> data, Context context, NotificationCardHolder.NotificationCardListener listener) {
        listData = data;
        mContext = context;
        this.listener = listener;

        if(listData == null){
            listData = new ArrayList<NotificationVO>();
        }
    }

    public Object getItem(int position) {
        if(listData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public NotificationCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NotificationCardHolder viewHolder;
        View notificationCard;

        if(viewType == TYPE_ITEM){
            notificationCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_notification, parent, false);
            viewHolder = new NotificationCardHolder(notificationCard, listener);
        }else{
            notificationCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new NotificationCardHolder(notificationCard, listener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NotificationCardHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                NotificationVO notification = listData.get(position);
                holder.setData(notification);
                break;
            case TYPE_EMPTY:
                holder.titleView.setText(mContext.getResources().getString(R.string.no_notifications_yet));
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
        if(getItem(position) instanceof NotificationVO){
            return TYPE_ITEM;
        }else{
            return TYPE_EMPTY;
        }
    }
}

