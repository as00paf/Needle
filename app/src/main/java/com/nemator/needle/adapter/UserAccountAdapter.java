package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.viewHolders.ListItemViewHolder;

import java.util.ArrayList;

public class UserAccountAdapter extends RecyclerView.Adapter<ListItemViewHolder>{

    public final String TAG = "UserAccountAdapter";

    private final int EMAIL_ACCOUNT = 0;
    private final int FACEBOOK_ACCOUNT = 1;
    private final int GOOGLE_ACCOUNT = 2;
    private final int TWITTER_ACCOUNT = 3;
    private final int ADD_REMOVE_FRIEND = 4;
    private final int ADD_TO_GROUP = 5;
    private final int SEND_LOCATION = 6;
    private final int SEND_MESSAGE = 7;

    private Context context;
    private UserVO user;
    private Boolean isMe;
    private ArrayList<Integer> positionTypes = new ArrayList<>();

    public UserAccountAdapter(Context context, UserVO user, Boolean isMe) {
        this.context = context;
        this.user = user;
        this.isMe = isMe;
        initItemPositions();
    }

    private void initItemPositions() {
        if(isMe){
            positionTypes.add(EMAIL_ACCOUNT);
            positionTypes.addAll(orderSocialAccounts());
        }else{
            positionTypes.add(ADD_REMOVE_FRIEND);
            positionTypes.add(ADD_TO_GROUP);
            positionTypes.add(SEND_LOCATION);
            positionTypes.add(SEND_MESSAGE);
        }
    }

    private ArrayList<Integer> orderSocialAccounts() {
        ArrayList<Integer> result = new ArrayList<>();
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_FACEBOOK) result.add(FACEBOOK_ACCOUNT);
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_TWITTER) result.add(TWITTER_ACCOUNT);
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_GOOGLE) result.add(GOOGLE_ACCOUNT);

        //if(!result.contains(FACEBOOK_ACCOUNT)) result.add(FACEBOOK_ACCOUNT);
        //if(!result.contains(TWITTER_ACCOUNT)) result.add(TWITTER_ACCOUNT);
        //if(!result.contains(GOOGLE_ACCOUNT)) result.add(GOOGLE_ACCOUNT);

        return result;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ListItemViewHolder viewHolder = new ListItemViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type){
            case EMAIL_ACCOUNT :
                holder.title.setText(context.getString(R.string.email));
                holder.subtitle.setText(user.getEmail());
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email_black_24dp));
                break;
            case FACEBOOK_ACCOUNT :
                holder.title.setText(context.getString(R.string.facebook_account));
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.com_facebook_blue));
                holder.subtitle.setText(user.getEmail());
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.facebook_logo));
                break;
            case TWITTER_ACCOUNT :
                holder.title.setText(context.getString(R.string.facebook_account));
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.tw__blue_default));
                holder.subtitle.setText(user.getEmail());
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.twitter_logo_white));
                break;
            case GOOGLE_ACCOUNT :
                holder.title.setText(context.getString(R.string.google_account_title));
                holder.subtitle.setText(user.getEmail());
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.common_google_signin_btn_icon_dark_normal));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click");
            }
        });
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
