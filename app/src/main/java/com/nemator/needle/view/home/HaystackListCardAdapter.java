package com.nemator.needle.view.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.view.haystack.HaystackActivity;
import com.nemator.needle.tasks.ImageDownloaderTask;
import com.nemator.needle.models.vo.HaystackVO;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class HaystackListCardAdapter extends RecyclerView.Adapter<HaystackListCardAdapter.HaystackCardViewHolder> {
    public static String TAG = "HaystackListCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;

    private ArrayList<HaystackVO> listData;
    private Context mContext;

    public HaystackListCardAdapter(ArrayList<HaystackVO> data, Context context) {
        listData = data;
        mContext = context;

        if(listData == null){
            listData = new ArrayList<HaystackVO>();
        }
    }

    public Object getItem(int position) {
        if(listData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public HaystackListCardAdapter.HaystackCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HaystackCardViewHolder viewHolder;
        View haystackCard;

        if(viewType == TYPE_ITEM){
            haystackCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_card_layout, parent, false);
            viewHolder = new HaystackCardViewHolder(haystackCard, true);
        }else{
            haystackCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new HaystackCardViewHolder(haystackCard, false);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HaystackCardViewHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                HaystackVO haystack = (HaystackVO) listData.get(position);
                holder.titleView.setText(haystack.getName());

                int count = haystack.getActiveUsers().size();
                String userCount = count + " " +mContext.getResources().getString(R.string.activeUsers);
                holder.userCountView.setText(userCount);

                String activeUntil = haystack.getTimeLimit();
                activeUntil = activeUntil.replace("00:00:00", "");
                activeUntil = activeUntil.replace(":00", "");
                holder.active_until.setText(activeUntil);

                if (holder.imageView != null && haystack.getPictureURL() != null) {
                    String encodedURL = AppConstants.HAYSTACK_PICTURES_URL + Uri.encode(haystack.getPictureURL());
                    new ImageDownloaderTask(holder.imageView).execute(encodedURL);
                }

                holder.setData(haystack);

                break;
            case TYPE_EMPTY:
                holder.emptyText.setText(mContext.getResources().getString(R.string.noHaystackAvailable));
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

    public static class HaystackCardViewHolder extends RecyclerView.ViewHolder{
        //Item
        TextView titleView, userCountView, active_until;
        ImageView imageView;

        //Empty
        TextView emptyText;

        HaystackVO haystackData;

        public HaystackCardViewHolder(View view, Boolean isNotEmpty) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.title);
            userCountView = (TextView)  view.findViewById(R.id.active_users);
            active_until = (TextView)  view.findViewById(R.id.active_until);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);

            /*if(isNotEmpty){
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Haystack haystackData = (Haystack) getItem(getPosition());
                        Intent intent = new Intent(view.getContext(), HaystackActivity.class);
                        intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystackData);
                        view.getContext().startActivity(intent);
                    }
                });
            }*/

        }

        public HaystackCardViewHolder(View view) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.title);
            userCountView = (TextView)  view.findViewById(R.id.active_users);
            active_until = (TextView)  view.findViewById(R.id.active_until);
            emptyText = (TextView) view.findViewById(R.id.emptyText);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }

        public void setData(HaystackVO haystack){
            haystackData = haystack;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), HaystackActivity.class);
                    intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystackData);
                    view.getContext().startActivity(intent);
                }
            });
        }
    }
}

