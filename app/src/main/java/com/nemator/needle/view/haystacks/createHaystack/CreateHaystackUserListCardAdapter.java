package com.nemator.needle.view.haystacks.createHaystack;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CreateHaystackUserListCardAdapter extends RecyclerView.Adapter<CreateHaystackUserListCardAdapter.UserCardViewHolder> {
    private ArrayList<UserVO> listData;
    private ArrayList<UserVO> filteredListData;
    private ArrayList<UserVO> selectedItems = new ArrayList<UserVO>();
    private Context mContext;

    public CreateHaystackUserListCardAdapter(ArrayList<UserVO> data, Context context) {
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

        userCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_haystack_user_card_layout, parent, false);
        viewHolder = new UserCardViewHolder(userCard);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserCardViewHolder holder, int position) {
        UserVO user = (UserVO) filteredListData.get(position);

        //User name
        holder.userNameView.setText(user.getUserName());

        //Image
        if(user.getPictureURL() != null){
            Picasso.with(mContext)
                    .load(user.getPictureURL())
                    .resize(250, 250)
                    .centerCrop()
                    .error(mContext.getResources().getDrawable(R.drawable.person_placeholder))
                    .into(holder.imageView);
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
            userNameView =  (TextView) view.findViewById(R.id.create_haystack_user_card_name_label);
            imageView = (ImageView) view.findViewById(R.id.create_haystack_user_card_image);

            view.setClickable(true);
        }

        public void setData(final UserVO user){
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

