package com.nemator.needle.viewHolders;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.adapter.UserAccountAdapter;
import com.nemator.needle.models.vo.UserVO;

public class UserProfileListViewHolder extends RecyclerView.ViewHolder{

    public RecyclerView listView;
    private UserVO user;

    public UserProfileListViewHolder(View itemView, UserVO user, Boolean isMe) {
        super(itemView);

        this.user = user;
        listView = (RecyclerView) itemView.findViewById(R.id.card_list_view);
        listView.setHasFixedSize(true);

        int orientation = isMe ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL;
        RecyclerView.LayoutManager manager = new LinearLayoutManager(itemView.getContext(), orientation, false);
        listView.setLayoutManager(manager);

        UserAccountAdapter adapter = new UserAccountAdapter(itemView.getContext(), user, isMe);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
