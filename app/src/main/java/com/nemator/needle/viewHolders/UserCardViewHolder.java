package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;
import com.squareup.picasso.Picasso;

public class UserCardViewHolder extends RecyclerView.ViewHolder{

    public static String TAG = "UserCardViewHolder";

    private final Delegate delegate;
    //Item
    public TextView userNameView, emptyText;
    public ImageView imageView;

    private UserVO userData;
    private FriendshipVO friendship;

    public UserCardViewHolder(View view, Delegate delegate) {
        super(view);
        this.delegate = delegate;
        userNameView =  (TextView) view.findViewById(R.id.username_label);
        imageView = (ImageView) view.findViewById(R.id.user_profile_picture);

        emptyText = (TextView) view.findViewById(R.id.title);

        view.setClickable(true);
    }

    public void setData(UserVO user, FriendshipVO friendshipVO){
        this.userData = user;
        this.friendship = friendshipVO;

        //User name
        userNameView.setText(user.getReadableUserName());

        //Image
        Picasso.with(itemView.getContext()).cancelRequest(imageView);

        if(user.getPictureURL() != null && !user.getPictureURL().isEmpty()){
            Picasso.with(itemView.getContext())
                    .load(user.getPictureURL())
                    .into(imageView);
        }else{
            Log.d(TAG, "Cant get picture URL for user " + user.getUserName());
            imageView.setImageResource(R.drawable.person_placeholder);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            delegate.onUserSelected(view, userData, friendship);

            if(view.isSelected()){
                view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(R.color.primary));
            }else{
                view.findViewById(R.id.username_label).setBackgroundColor(view.getContext().getResources().getColor(android.R.color.transparent));
            }
            }
        });
    }

    public interface Delegate{
        void onUserSelected(View view, UserVO user, FriendshipVO friendship);
    }
}
