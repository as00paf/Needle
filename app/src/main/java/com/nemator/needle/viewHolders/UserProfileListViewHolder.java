package com.nemator.needle.viewHolders;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nemator.needle.R;
import com.nemator.needle.adapter.UserAccountAdapter;
import com.nemator.needle.interfaces.IUserProfileListener;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;

public class UserProfileListViewHolder extends RecyclerView.ViewHolder{

    public RecyclerView listView;
    private UserVO user;
    private boolean isMe;
    private FriendshipVO friendship;
    private IUserProfileListener listener;

    public UserProfileListViewHolder(View itemView, UserVO user, boolean isMe, FriendshipVO friendship, IUserProfileListener listener) {
        super(itemView);
        this.user = user;
        this.isMe = isMe;
        this.friendship = friendship;
        this.listener = listener;

        init();
    }

    private void init() {
        listView = (RecyclerView) itemView.findViewById(R.id.card_list_view);
        listView.setHasFixedSize(true);

        RecyclerView.LayoutManager manager;
        if(isMe){
            manager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false);
        }else{
            manager = new GridLayoutManager(itemView.getContext(), 3);
            listView.setHasFixedSize(true);
        }
        listView.setLayoutManager(manager);

        UserAccountAdapter adapter = new UserAccountAdapter(itemView.getContext(), user, isMe, friendship, listener);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
