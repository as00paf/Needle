package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.viewHolders.ListItemViewHolder;
import com.nemator.needle.viewHolders.UserProfileListViewHolder;

import java.util.ArrayList;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public final String TAG = "UserProfileAdapter";

    private final int ACCOUNT_DETAILS = 0;
    private final int DELETE_ACCOUNT = 1;

    private Context context;
    private UserVO user;
    private Boolean isMe;
    private ArrayList<Integer> positionTypes = new ArrayList<>();

    public UserProfileAdapter(Context context, UserVO user, Boolean isMe) {
        this.context = context;
        this.user = user;
        this.isMe = isMe;

        initItemPositions();
    }

    private void initItemPositions() {
        positionTypes.add(ACCOUNT_DETAILS);

        if(isMe){
            positionTypes.add(DELETE_ACCOUNT);
        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View itemView;

        switch (viewType){
            case ACCOUNT_DETAILS:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_profile_list_item, parent, false);
                viewHolder = new UserProfileListViewHolder(itemView, user, isMe);
                break;
            case DELETE_ACCOUNT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_profile_item, parent, false);
                viewHolder = new ListItemViewHolder(itemView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type){
            case ACCOUNT_DETAILS:
                break;
            case DELETE_ACCOUNT :
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Click");
                    }
                });

                ((ListItemViewHolder) holder).title.setText(context.getString(R.string.delete_account));
                ((ListItemViewHolder) holder).title.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                ((ListItemViewHolder) holder).subtitle.setText(context.getString(R.string.delete_account_info));
                ((ListItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_account));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return positionTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return positionTypes.get(position);
    }
}
