package com.nemator.needle.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserListCardAdapter extends RecyclerView.Adapter<UserListCardAdapter.UserCardViewHolder> {
    public static final String TAG = "UserListAdapter";

    private ArrayList<UserVO> listData;
    private ArrayList<UserVO> filteredListData;
    private ArrayList<UserVO> selectedItems = new ArrayList<UserVO>();
    private UserVO selectedItem;
    private Context mContext;

    public UserListCardAdapter(ArrayList<UserVO> data, Context context) {
        listData = data;
        filteredListData = data;
        mContext = context;

        if(listData == null){
            listData = new ArrayList<UserVO>();
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
        return filteredListData.get(position);
    }

    @Override
    public UserCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserCardViewHolder viewHolder;
        View userCard;

        userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user, parent, false);
        viewHolder = new UserCardViewHolder(userCard);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserCardViewHolder holder, int position) {
        UserVO user = (UserVO) filteredListData.get(position);

        //User name
        holder.userNameView.setText(user.getUserName());

        int padding = 8 * 2;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels / 2 - padding;

        holder.itemView.getLayoutParams().height  = width;

        //Image
        if(user.getPictureURL() != null && !user.getPictureURL().isEmpty()){
            Picasso.with(mContext)
                    .load(user.getPictureURL())
                    .resize(width,width)
                    .into(holder.imageView);
        }else{
            Log.d(TAG, "Cant get picture URL for user " + user.getUserName());
        }

        holder.setData(user);
    }

    @Override
    public int getItemCount() {
        return filteredListData.size();
    }

    public class UserCardViewHolder extends RecyclerView.ViewHolder{
        //Item
        TextView userNameView;
        ImageView imageView;

        UserVO userData;

        public UserCardViewHolder(View view) {
            super(view);
            userNameView =  (TextView) view.findViewById(R.id.username_label);
            imageView = (ImageView) view.findViewById(R.id.user_profile_picture);

            view.setClickable(true);
        }

        public void setData(final UserVO user){
            userData = user;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setSelected(!view.isSelected());

                    if(view.isSelected()){
                        view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(R.color.primary));
                        selectedItems.add(userData);
                        selectedItem = userData;
                    }else{
                        view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(android.R.color.transparent));
                        selectedItems.remove(userData);
                        selectedItem = null;
                    }
                }
            });
        }
    }
}

