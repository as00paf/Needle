package com.nemator.needle.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.activities.UserProfileActivity;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.LocationCardHolder;
import com.nemator.needle.viewHolders.UserCardViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserCardAdapter extends RecyclerView.Adapter<UserCardViewHolder> {
    public static final String TAG = "UserListAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 1;

    private ArrayList<UserVO> listData;
    private ArrayList<UserVO> filteredListData;
    private ArrayList<FriendshipVO> friendshipData;
    private ArrayList<UserVO> selectedItems = new ArrayList<UserVO>();
    private UserVO selectedItem;
    private Context mContext;
    private int type;

    public UserCardAdapter(Context context, ArrayList<UserVO> data, int type) {
        listData = data;
        filteredListData = data;
        mContext = context;
        this.type = type;

        if(listData == null){
            listData = new ArrayList<UserVO>();
            filteredListData = new ArrayList<UserVO>();
        }
    }

    public UserCardAdapter(Context context, ArrayList<UserVO> data, ArrayList<FriendshipVO> friendshipData, int type) {
        this.listData = data;
        filteredListData = data;
        this.friendshipData = friendshipData;
        this.type = type;
        this.mContext = context;

        if(listData == null){
            listData = new ArrayList<UserVO>();
            filteredListData = new ArrayList<UserVO>();
        }
    }

    public ArrayList<UserVO> getSelectedItems(){
        return selectedItems;
    }

    public void flushFilter(){
        filteredListData = new ArrayList<>();
        filteredListData.addAll(listData);
        notifyDataSetChanged();
    }

    public void setFilter(String queryText) {
        filteredListData = new ArrayList<>();

        for (UserVO item: listData) {
            if (item.getUserName().toLowerCase().contains(queryText.toLowerCase()))
                filteredListData.add(item);
        }
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        if(filteredListData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public UserCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserCardViewHolder viewHolder;
        View userCard;

        if(viewType == TYPE_ITEM){
            userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_new, parent, false);
            viewHolder = new UserCardViewHolder(userCard, delegate);
        }else{
            userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new UserCardViewHolder(userCard, delegate);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserCardViewHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                UserVO user = filteredListData.get(position);
                holder.setData(user, null);
                break;
            case TYPE_EMPTY :
                holder.emptyText.setText(mContext.getResources().getString(R.string.no_one_here));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(filteredListData == null || filteredListData.size() == 0) return 1;

        return filteredListData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) instanceof UserVO){
            return TYPE_ITEM;
        }else{
            return TYPE_EMPTY;
        }
    }

    private UserCardViewHolder.Delegate delegate = new UserCardViewHolder.Delegate() {
        private View lastSelected;

        @Override
        public void onUserSelected(View view, UserVO user, FriendshipVO friendshipVO) {
            if(type == Type.SHOW_PROFILE){
                Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                intent.putExtra(AppConstants.TAG_USER, (Parcelable) user);
                intent.putExtra(AppConstants.TAG_FRIENDSHIP, (Parcelable) friendshipVO);
                view.getContext().startActivity(intent);
                return;
            }

            if(type == Type.SINGLE_SELECT){
                if(lastSelected != null){
                    lastSelected.setSelected(false);
                    lastSelected.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(android.R.color.transparent));
                }
                lastSelected = view;
            }

            view.setSelected(!view.isSelected());

            if(view.isSelected()){
                selectedItems.add(user);
                selectedItem = user;
            }else{
                selectedItems.remove(user);
                selectedItem = null;
                lastSelected = null;
            }
        }
    };

    public static class Type{
        public static int SINGLE_SELECT = 0;
        public static int MULTI_SELECT = 1;
        public static int SHOW_PROFILE = 2;
    }
}

