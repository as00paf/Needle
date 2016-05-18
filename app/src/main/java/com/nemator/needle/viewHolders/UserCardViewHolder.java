package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;

public class UserCardViewHolder extends RecyclerView.ViewHolder{
    private final Delegate delegate;
    //Item
    public TextView userNameView;
    public ImageView imageView;

    private UserVO userData;

    public UserCardViewHolder(View view, Delegate delegate) {
        super(view);
        this.delegate = delegate;
        userNameView =  (TextView) view.findViewById(R.id.username_label);
        imageView = (ImageView) view.findViewById(R.id.user_profile_picture);

        view.setClickable(true);
    }

    public void setData(final UserVO user){
        userData = user;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            delegate.onUserSelected(view, userData);

            if(view.isSelected()){
                view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(R.color.primary));
            }else{
                view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(android.R.color.transparent));
            }
            }
        });
    }

    public interface Delegate{
        void onUserSelected(View view, UserVO user);
    }
}
