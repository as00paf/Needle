package com.nemator.needle.viewHolders;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nemator.needle.R;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class FriendListViewHolder extends RecyclerView.ViewHolder{

    public RecyclerView listView;
    private ArrayList<UserVO> friends;

    public FriendListViewHolder(View itemView) {
        super(itemView);
    }

    public void setFriends(ArrayList<UserVO> friends) {
        this.friends = friends;
        init();
    }

    private void init() {
        listView = (RecyclerView) itemView.findViewById(R.id.card_list_view);
        listView.setHasFixedSize(true);

        UserCardAdapter adapter = new UserCardAdapter(itemView.getContext(), friends, UserCardAdapter.Type.SHOW_PROFILE);
        listView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new GridLayoutManager(itemView.getContext(), 3);
        listView.setLayoutManager(manager);


        adapter.notifyDataSetChanged();
    }
}
