package com.nemator.needle.home;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.haystack.HaystackActivity;
import com.nemator.needle.home.task.ImageDownloaderTask;
import com.nemator.needle.models.Haystack;

import java.util.ArrayList;
import java.util.TreeSet;

public class HaystackListCardAdapter extends RecyclerView.Adapter<HaystackListCardAdapter.HaystackCardViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;

    private ArrayList<Haystack> listData;
    private Context mContext;

    public HaystackListCardAdapter(ArrayList<Haystack> data, Context context) {
        listData = data;
        mContext = context;

        if(listData == null){
            listData = new ArrayList<Haystack>();
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
                Haystack haystack = (Haystack) listData.get(position);
                holder.titleView.setText(haystack.getName());

                int count = haystack.getActiveUsers().size();
                String userCount = count + " " +mContext.getResources().getString(R.string.activeUsers);
                holder.userCountView.setText(userCount);

                String activeUntil = mContext.getResources().getString(R.string.activeUntil)+ " "+haystack.getTimeLimit();
                activeUntil = activeUntil.replace(" 00:00:00", "");
                holder.active_until.setText(activeUntil);

                if (holder.imageView != null) {
                    new ImageDownloaderTask(holder.imageView).execute(haystack.getPictureURL());
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
        if(getItem(position) instanceof Haystack){
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

        Haystack haystackData;

        public HaystackCardViewHolder(View view, Boolean isNotEmpty) {
            super(view);
            titleView =  (TextView) view.findViewById(R.id.title);
            userCountView = (TextView)  view.findViewById(R.id.active_users);
            active_until = (TextView)  view.findViewById(R.id.active_until);
            emptyText = (TextView) view.findViewById(R.id.emptyText);

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
        }

        public void setData(Haystack haystack){
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

