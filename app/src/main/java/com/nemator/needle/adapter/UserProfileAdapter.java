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
import com.nemator.needle.interfaces.IUserProfileListener;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.viewHolders.FriendListViewHolder;
import com.nemator.needle.viewHolders.ListItemViewHolder;
import com.nemator.needle.viewHolders.UserProfileListViewHolder;

import java.util.ArrayList;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public final String TAG = "UserProfileAdapter";

    private final int ACCOUNT_DETAILS = 0;
    private final int DELETE_ACCOUNT = 1;
    private final int FRIENDS = 2;
    private final int HAYSTACKS = 3;

    private Context context;
    private UserVO user;
    private ArrayList<UserVO> friends;
    private Boolean isMe;
    private FriendshipVO friendship;
    private ArrayList<Integer> positionTypes = new ArrayList<>();
    private IUserProfileListener listener;

    public UserProfileAdapter(Context context, UserVO user, ArrayList<UserVO> friends, Boolean isMe, FriendshipVO friendship, IUserProfileListener listener) {
        this.context = context;
        this.user = user;
        this.friends = friends;
        this.isMe = isMe;
        this.friendship = friendship;
        this.listener = listener;

        initItemPositions();
    }

    private void initItemPositions() {
        if(isMe){
            positionTypes.add(FRIENDS);
            positionTypes.add(ACCOUNT_DETAILS);
            positionTypes.add(DELETE_ACCOUNT);
        }else{
            positionTypes.add(ACCOUNT_DETAILS);
            positionTypes.add(FRIENDS);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View itemView;

        switch (viewType){
            case ACCOUNT_DETAILS:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_profile_list_item, parent, false);
                viewHolder = new UserProfileListViewHolder(itemView, user, isMe, friendship, listener);
                break;
            case DELETE_ACCOUNT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_profile_item, parent, false);
                viewHolder = new ListItemViewHolder(itemView);
                break;
            case FRIENDS:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_profile_friends_item, parent, false);
                viewHolder = new FriendListViewHolder(itemView);
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
            case FRIENDS:
                ((FriendListViewHolder) holder).setFriends(friends);
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
