package com.nemator.needle.home.createHaystack;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
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
import com.nemator.needle.models.User;

import java.util.ArrayList;

public class CreateHaystackUserListCardAdapter extends RecyclerView.Adapter<CreateHaystackUserListCardAdapter.UserCardViewHolder> {
    private ArrayList<User> listData;
    private ArrayList<User> filteredListData;
    private ArrayList<User> selectedItems = new ArrayList<User>();
    private Context mContext;

    public CreateHaystackUserListCardAdapter(ArrayList<User> data, Context context) {
        listData = data;
        filteredListData = data;
        mContext = context;

        if(listData == null){
            listData = new ArrayList<User>();
        }
    }

    public ArrayList<User> getSelectedItems(){
        return selectedItems;
    }

    public void flushFilter(){
        filteredListData = new ArrayList<>();
        filteredListData.addAll(listData);
        notifyDataSetChanged();
    }

    public void setFilter(String queryText) {
        filteredListData = new ArrayList<>();

        for (User item: listData) {
            if (item.getUserName().toLowerCase().contains(queryText))
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

        userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_haystack_user_card_layout, parent, false);
        viewHolder = new UserCardViewHolder(userCard);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserCardViewHolder holder, int position) {
        User user = (User) filteredListData.get(position);

        //User name
        holder.userNameView.setText(user.getUserName());

        //Image
        if(user.getPictureURL() != null){
            new ImageDownloaderTask(holder.imageView).execute(user.getPictureURL());
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

        User userData;

        public UserCardViewHolder(View view) {
            super(view);
            userNameView =  (TextView) view.findViewById(R.id.create_haystack_user_card_name_label);
            imageView = (ImageView) view.findViewById(R.id.create_haystack_user_card_image);

            view.setClickable(true);
        }

        public void setData(final User user){
            userData = user;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setSelected(!view.isSelected());

                    if(view.isSelected()){
                        view.findViewById(R.id.create_haystack_user_card_name_label).setBackgroundColor(view.getContext().getResources().getColor(R.color.primary));
                        selectedItems.add(user);
                    }else{
                        view.findViewById(R.id.create_haystack_user_card_name_label).setBackgroundColor(0xCCFFFFFF);
                        selectedItems.remove(user);
                    }
                }
            });
        }
    }
}

